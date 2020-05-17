package com.datadog.pej.kafka;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class SpringKafkaApplication {


  private static final Logger log = LoggerFactory.getLogger(SpringKafkaApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(SpringKafkaApplication.class, args);
  }


/* To be used with the -javaagent


  public static Tracer initTracer(@Value("ServiceKafka") String service){
    Tracer tracer = GlobalTracer.get();
    return tracer;
  }
*/

  @Bean
  public RestTemplate restTemplate(){
    return new RestTemplateBuilder().build();
  }


  @Bean
  public CommandLineRunner run() {
    return args -> {

      //sender.send("Un message depuis command line runner");
      //receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);

      log.info("\ntest");
    };
  }

}
