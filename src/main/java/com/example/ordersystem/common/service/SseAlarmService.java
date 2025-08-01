package com.example.ordersystem.common.service;


import com.example.ordersystem.common.dto.SseMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final RedisTemplate redisTemplate;

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, @Qualifier("ssePubSub") RedisTemplate redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.redisTemplate = redisTemplate;
    }

    //    특정사용자에게 메시지 발송하겠다.(productID 부분이 내가 보내줄 자료)
    public void publishMessage(String receiver, String sender, Long orderingId) {
        SseMessageDto dto = SseMessageDto.builder()
                .sender(sender)
                .orderingID(orderingId)
                .receiver(receiver)
                .build();

        String data = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            data = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

//        emmiter객체를 통해 메시지 전송
//        우리는 Map에 관리하고 거기에 있는 받는 사람(key값) 에 해당하는 value를 sseEmitter에 담음
    SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
//        emitter 객체가 현재 서버에 있으면 직접 알림 발송, 그렇지 않으면 redis에 publish
        if(sseEmitter != null) {


            try {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//        사용자가 로그아웃후 또는 새로고침에 다시 화면에 들어왔을 때 알림메시지가 남아있으려면 DB에 추가적으로 저장 필요.
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
//            발송
            redisTemplate.convertAndSend("order-channel" , data);

        }

    }


    @Override
    public void onMessage(Message message, byte[] pattern) {
//        Message : 실질적인 메시지가 담겨있는 객체
//        pattern : 채널명

//        여러개의 채널을 구독하고 있을 경우 채널명으로 분기처리
        String channelName = new String(pattern);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);

            //        emmiter객체를 통해 메시지 전송
            //        우리는 Map에 관리하고 거기에 있는걸로 SseEmitter를 ?
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());

            //        emitter 객체가 현재 서버에 있으면 직접 알림 발송, 그렇지 않으면 redis에 publish
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("ordered").data(dto));
                    //        사용자가 로그아웃후 또는 새로고침에 다시 화면에 들어왔을 때 알림메시지가 남아있으려면 DB에 추가적으로 저장 필요.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println(channelName);
            System.out.println("dto : " + dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
