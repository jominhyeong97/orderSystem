package com.example.ordersystem.common.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;
     @Value("${spring.rabbitmq.port}")
    private int port;
     @Value("${spring.rabbitmq.username}")
    private String username;
     @Value("${spring.rabbitmq.password}")
    private String password;
     @Value("${spring.rabbitmq.virtualhost}")
    private String virtualhost;

//     스프링빈 생성을 통해 rabbitMq에 자동으로 아래 변수명으로 큐가 생성

    @Bean
    public Queue stockQueue() {
        return new Queue("stockDecreaseQueue", true);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualhost);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter()); //json으로 넣겠다.
        return rabbitTemplate;
    }


}
