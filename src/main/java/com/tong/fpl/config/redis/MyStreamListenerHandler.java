package com.tong.fpl.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Create by tong on 2021/6/11
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyStreamListenerHandler implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        // 消息ID
        RecordId messageId = message.getId();
        // 消息的key和value
        Map<String, String> body = message.getValue();
        log.info("stream message。messageId={}, stream={}, body={}", messageId, message.getStream(), body);
        // 通过RedisTemplate手动确认消息
        this.stringRedisTemplate.opsForStream().acknowledge("mystream", message);
    }

}
