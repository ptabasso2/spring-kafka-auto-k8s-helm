# spring-kafka-auto-k8s-helm

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.codenotfound%3Aspring-kafka-hello-world&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.codenotfound%3Aspring-kafka-hello-world)


A detailed step-by-step tutorial on how to implement an Apache Kafka Consumer and Producer using Spring Kafka and Spring Boot in a kubernetes infrastructure (GKE)
<br>In this tutorial we will also enable distributed tracing using the Datadog java agent (automatic instrumentation).
This tutorial assumes that 
+ You already have access to a kubernetes cluster. The example below has been tested on GKE. But you can replicate the same instructions on any type of clusters managed or not.
+ Helm tiller is deployed on the cluster (see instructions below)
+ The Kubernetes Datadog cluster agent has already been deployed (Either through a Daemonset or Helm). Note that there is a new way of deploying the Datadog agent relying k8s operator (See detailed instructions: [Kubernetes](https://docs.datadoghq.com/agent/kubernetes/?tab=helm))
  

### _Preliminary tasks_

If you don't need to change anything in the application code or in the docker image, you may skip the two next steps and go straight to _Creating services and pods in the cluster_
A docker image is already available in Docker hub (`docker pull pejdd/springkafka:v0`). 

**(Optional) Building the the spring application**

```
COMP10619:pejman.tabassomi$ ./gradlew build
```


**(Optional) Building the Docker image that will contain the spring application and pushing it to the remote registry**

```
COMP10619:pejman.tabassomi$ docker build -f Dockerfile.springkafka -t <account>/<repo>:<tag> .
COMP10619:pejman.tabassomi$ docker login -u=<username> -p=<password>
COMP10619:pejman.tabassomi$ docker push <account or username>/<repo>>:<tag>
```

***Important***: If you plan to use a custom docker image, you would consequently need to update the corresponding section in **values.yaml** file located in the helm directory:<br>



```
image:
  repository: <account>/<repository> 
  tag: <tag>
  pullPolicy: IfNotPresent
```

**Deploy helm tiller on the cluster**

```
COMP10619:pejman.tabassomi$ curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get | bash
COMP10619:pejman.tabassomi$ kubectl get sa --namespace kube-system
COMP10619:pejman.tabassomi$ kubectl --namespace kube-system create sa tiller
COMP10619:pejman.tabassomi$ kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
COMP10619:pejman.tabassomi$ helm init --service-account tiller
COMP10619:pejman.tabassomi$ helm repo update
COMP10619:pejman.tabassomi$ kubectl get deploy,svc tiller-deploy -n kube-system
```

**Creating services and pods in the cluster using helm**

***Note:*** At the time of the writing, helm version 2.16 is being used 

Running the following will display the set of manifest files that will be used to create the application on the cluster.

```
COMP10619:pejman.tabassomi$ helm install --dry-run --debug --name sk ./helm/
```

Now pushing it
```
COMP10619:pejman.tabassomi$ helm install --name sk ./helm/
```



**Checking if everyting is ok on the cluster**

If everything is fine we should get the following result

```
COMP10619:pejman.tabassomi$ helm ls
NAME	REVISION	UPDATED                 	STATUS  	CHART            	APP VERSION	NAMESPACE
sk  	1       	Mon May 18 07:34:44 2020	DEPLOYED	springkafka-0.1.0	1.0        	default  
```

```
COMP10619:pejman.tabassomi$ kubectl get svc --output=wide
NAME          TYPE           CLUSTER-IP      EXTERNAL-IP     PORT(S)                      AGE     SELECTOR
kafka         ClusterIP      10.228.4.130    <none>          9092/TCP                     6d1h    name=kafka
kubernetes    ClusterIP      10.228.0.1      <none>          443/TCP                      234d    <none>
springkafka   LoadBalancer   10.228.0.103    34.89.190.246   8088:32699/TCP               4d8h    app=springkafka
zookeeper     ClusterIP      10.228.4.213    <none>          2181/TCP,2888/TCP,3888/TCP   6d1h    name=zookeeper
```

As we can see, the three services *zookeeper*, *kafka* and *springkafka* are up and running.
<br>The next list shows up the running pods and we can see that the three components (zookeeper, kafka and spring) are also running.  

```
COMP10619:pejman.tabassomi$ kubectl get pod --output=wide
NAME                           READY   STATUS    RESTARTS   AGE     IP           NODE                                                  NOMINATED NODE   READINESS GATES
datadog-agent-5q4xx            1/1     Running   0          20d     10.36.1.2    gke-pejman-cluster--default-pool-02e93412-gjvl   <none>           <none>
datadog-agent-gd27x            1/1     Running   0          20d     10.36.0.2    gke-pejman-cluster--default-pool-02e93412-d2j5   <none>           <none>
datadog-agent-kkxwc            1/1     Running   0          20d     10.36.2.2    gke-pejman-cluster--default-pool-02e93412-lbl8   <none>           <none>
kafka-7556ddfc4b-wm859         1/1     Running   0          6d1h    10.36.2.27   gke-pejman-cluster--default-pool-02e93412-lbl8   <none>           <none>
springkafka-6b5b79f778-xmx56   1/1     Running   0          4d8h    10.36.1.16   gke-pejman-cluster--default-pool-02e93412-gjvl   <none>           <none>
zookeeper-74cd747874-p7gg8     1/1     Running   0          6d1h    10.36.2.26   gke-pejman-cluster--default-pool-02e93412-lbl8   <none>           <none>

```


### _Checking connectivity from the internet to the front loadbalancer (springkafka service)_

That applies if you run the application on a managed platform (ex EKS, AKS GKE).
On GKE for instance, one would need to whitelist the IP address range that is allowed to access the LB.
<br>By default no connection is allowed. Changing the Firewall settings might be necessary.
Once done, we can test the application.

### _Run the tests_

Open a new terminal window and run the following curl command **multiple times**:

```
COMP10619:Kafka pejman.tabassomi$ curl 34.89.190.246:8088/test
test
```

In this example this is the external IP of the loadbalancer (springkafka) that is being used.