package com.app.bulk_data_processing.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ImportJobProducer {

    private final RabbitTemplate rabbitTemplate;

    public ImportJobProducer(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(UUID jobId){

        rabbitTemplate.convertAndSend(
                "import.exchange",
                "import.job",
                jobId.toString()
        );
    }
}
