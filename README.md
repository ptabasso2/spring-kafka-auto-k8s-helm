# spring-kafka-auto

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.codenotfound%3Aspring-kafka-hello-world&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.codenotfound%3Aspring-kafka-hello-world)


A detailed step-by-step tutorial on how to implement an Apache Kafka Consumer and Producer using Spring Kafka and Spring Boot in a kubernetes infrastructure (GKE)
<br>In this tutorial we will also enable distributed tracing using the Datadog java agent (automatic instrumentation).
This tutorial assumes that 
+ You already have access to a kubernetes cluster. The example below has been tested on GKE.
+ The Kubernetes daemonset has already been deployed 

### _Preliminary tasks_

If you don't need to change anything in the application code, you may skip the two next steps and go straight to _Creating services and pods in the cluster_
An docker image is already available in Docker hub. 

**Building the the spring application**

```
COMP10619:pejman.tabassomi$ ./gradlew build
```


**Building the Docker image that will contain the spring application and pushing it to the remote registry**

```
COMP10619:pejman.tabassomi$ docker build -f Dockerfile.springkafka -t pejdd/springkafka:v0 .
COMP10619:pejman.tabassomi$ docker login -u=pejdd -p=<password>
COMP10619:pejman.tabassomi$ docker push pejdd/springkafka:v0
```

**Creating services and pods in the cluster**

The following manifest files will be used to build our environment:

+ zookeeper-service.yml
+ zookeeper-deployment.yml
+ kafka-service.yml
+ kafka-deployment.yml
+ springkafka.yml


```
COMP10619:pejman.tabassomi$ kubectl apply -f zookeeper-service.yml
COMP10619:pejman.tabassomi$ kubectl apply -f zookeeper-deployment.yml
COMP10619:pejman.tabassomi$ kubectl apply -f kafka-service.yml
COMP10619:pejman.tabassomi$ kubectl apply -f kafka-deployment.yml
COMP10619:pejman.tabassomi$ kubectl apply -f springkafka.yml
```

**Checking if everyting is ok on the cluster**

```
COMP10619:pejman.tabassomi$ kubectl get svc --output=wide
NAME          TYPE           CLUSTER-IP      EXTERNAL-IP     PORT(S)                      AGE     SELECTOR
kafka         ClusterIP      10.228.4.130    <none>          9092/TCP                     6d1h    name=kafka
kubernetes    ClusterIP      10.228.0.1      <none>          443/TCP                      234d    <none>
springkafka   LoadBalancer   10.228.0.103    34.89.190.246   8088:32699/TCP               4d8h    app=springkafka
web           NodePort       10.228.3.68     <none>          8080:30889/TCP               56d     run=web
web2          NodePort       10.228.0.50     <none>          8080:32507/TCP               56d     run=web2
zookeeper     ClusterIP      10.228.4.213    <none>          2181/TCP,2888/TCP,3888/TCP   6d1h    name=zookeeper
```

As we can see, the three services *zookeeper*, *kafka* and *springkafka* are up and running.
<br>The next list shows up the running pods and we can see that the three components (zookeeper, kafka and spring) are also running.  

```
COMP10619:pejman.tabassomi$ kubectl get pod --output=wide
NAME                           READY   STATUS    RESTARTS   AGE     IP           NODE                                                  NOMINATED NODE   READINESS GATES
datadog-agent-5q4xx            1/1     Running   0          20d     10.36.1.2    gke-pejman-sncf-cluster--default-pool-02e93412-gjvl   <none>           <none>
datadog-agent-gd27x            1/1     Running   0          20d     10.36.0.2    gke-pejman-sncf-cluster--default-pool-02e93412-d2j5   <none>           <none>
datadog-agent-kkxwc            1/1     Running   0          20d     10.36.2.2    gke-pejman-sncf-cluster--default-pool-02e93412-lbl8   <none>           <none>
kafka-7556ddfc4b-wm859         1/1     Running   0          6d1h    10.36.2.27   gke-pejman-sncf-cluster--default-pool-02e93412-lbl8   <none>           <none>
springkafka-6b5b79f778-xmx56   1/1     Running   0          4d8h    10.36.1.16   gke-pejman-sncf-cluster--default-pool-02e93412-gjvl   <none>           <none>
web-77656d79f8-2bffh           1/1     Running   0          20d     10.36.0.5    gke-pejman-sncf-cluster--default-pool-02e93412-d2j5   <none>           <none>
web2-675cf6d7b9-rfdg4          1/1     Running   0          20d     10.36.0.6    gke-pejman-sncf-cluster--default-pool-02e93412-d2j5   <none>           <none>
zookeeper-74cd747874-p7gg8     1/1     Running   0          6d1h    10.36.2.26   gke-pejman-sncf-cluster--default-pool-02e93412-lbl8   <none>           <none>

```

**Create a Topic "users" (First time)**

```
COMP10619:pejman.tabassomi$ kubectl exec -it kafka-7556ddfc4b-wm859 -- bash 
bash-4.4# cd /opt/kafka/bin
bash-4.4# ./kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic users
bash-4.4# ./kafka-topics.sh --list --zookeeper zookeeper:2181
__consumer_offsets
users
```

### _Checking connectivity from the internet to the front loadbalancer (springkafka service)_

On GKE for instance, one would need to whitelist the IP address range that is allowed to access the LB.
<br>By default no connection is allowed. Changing the Firewall settings might be necessary.   

### _Run the tests_

Open a new terminal window and run the following curl command:

```
COMP10619:Kafka pejman.tabassomi$ curl 34.89.190.246:8088/test
test
```

In this example this is the external IP of the loadbalancer (springkafka) that is being used.