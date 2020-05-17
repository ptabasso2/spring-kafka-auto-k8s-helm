package com.datadog.pej.kafka;

import com.datadog.pej.kafka.consumer.Receiver;
import com.datadog.pej.kafka.producer.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class KafkaController {

    @Autowired
    private Sender sender;

    @RequestMapping("/test")
    public String index() {
        sender.send("Un message plus long");
        return "\ntest";
    }


}
