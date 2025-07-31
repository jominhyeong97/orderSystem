package com.example.ordersystem.common.service;


import com.example.ordersystem.common.dto.StockRabbitMqDto;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@Component
public class StockRabbitMQService {
    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;

//    rabbitmq에 메시지 말행

    public void publish(Long productId, int productCount) {
        StockRabbitMqDto dto = StockRabbitMqDto.builder()
                .productCount(productCount)
                .productId(productId)
                .build();
        rabbitTemplate.convertAndSend("stockDecreaseQueue", dto);
    }
//    rabbitmq에 발행된 메시지를 수신
    @Transactional
//    Listenner는 싱글스레드로 메시지를 처리하므로 동시성이슈 발생 x
    @RabbitListener(queues = "stockDecreaseQueue")
    public void subscribe(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        StockRabbitMqDto dto = objectMapper.readValue(messageBody, StockRabbitMqDto.class);
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("없습니다."));
        product.updateStockQuantity(dto.getProductCount());
        System.out.println(messageBody);
    }

}
