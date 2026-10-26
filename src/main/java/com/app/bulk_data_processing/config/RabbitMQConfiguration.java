package com.app.bulk_data_processing.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange("import.exchange");
    }

    @Bean
    public Queue queue(){
        return new Queue("import.queue", true);
    }

    @Bean
    public Binding binding(Queue queue,
                           DirectExchange directExchange){

        return BindingBuilder.bind(queue)
                .to(directExchange)
                .with("import.job");
    }
}
