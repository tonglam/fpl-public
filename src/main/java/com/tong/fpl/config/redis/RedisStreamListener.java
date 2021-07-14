package com.tong.fpl.config.redis;

import com.tong.fpl.constant.enums.RedisExpirationKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

/**
 * Create by tong on 2021/6/30
 */
@Slf4j
//@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisStreamListener implements ApplicationRunner, DisposableBean {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisStreamListenerService redisStreamListenerService;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Override
    public void run(ApplicationArguments args) {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer
                .create(this.redisConnectionFactory,
                        StreamMessageListenerContainer
                                .StreamMessageListenerContainerOptions
                                .builder()
                                .errorHandler(t -> log.error("redis msg listener error, e:{}", t.getMessage()))
                                .pollTimeout(Duration.ZERO)
                                .build()
                );
        Consumer consumer = Consumer.from("fpl", "fpl-1");
        container.receive(consumer, StreamOffset.fromStart(StringUtils.joinWith("::", "Stream", RedisExpirationKey.EventAfterDeadline.name())), this.redisStreamListenerService);
//        container.receive(consumer, StreamOffset.create(StringUtils.joinWith("::", "Stream", RedisExpirationKey.EventMatchDay.name()), ReadOffset.lastConsumed()), this.redisStreamListenerService);
//        container.receive(consumer, StreamOffset.create(StringUtils.joinWith("::", "Stream", RedisExpirationKey.EventMatch.name()), ReadOffset.lastConsumed()), this.redisStreamListenerService);
        this.container = container;
        this.container.start();
    }

    @Override
    public void destroy() {
        this.container.stop();
    }

}
