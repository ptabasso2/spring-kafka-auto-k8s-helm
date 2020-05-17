package com.datadog.pej.kafka.consumer;

import java.util.concurrent.CountDownLatch;

import com.datadog.pej.kafka.Quote;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestTemplate;



public class Receiver {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(Receiver.class);

  private CountDownLatch latch = new CountDownLatch(1);

  @Autowired
  RestTemplate restTemplate;


  public CountDownLatch getLatch() {
    return latch;
  }

  @KafkaListener(topics = "users")
  public void receive(String payload, ConsumerRecord<?,?> cr) {

      LOGGER.info("received payload='{}'", payload);
      Quote quote = restTemplate.getForObject("https://gturnquist-quoters.cfapps.io/api/random", Quote.class);
      LOGGER.info(quote.toString());
      //latch.countDown();

      String result = restTemplate.getForObject("https://www.google.fr", String.class);
      LOGGER.info(result);
      latch.countDown();


  }
}
