package com.tong.fpl.config.redis;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Create by tong on 2021/6/29
 */
@Component
public class MyStreamConsumerListener implements ApplicationRunner, DisposableBean {

    private final RedisConnectionFactory redisConnectionFactory;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final MyStreamListenerHandler streamMessageListener;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer;

    public MyStreamConsumerListener(RedisConnectionFactory redisConnectionFactory, ThreadPoolTaskExecutor threadPoolTaskExecutor, MyStreamListenerHandler streamMessageListener) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.streamMessageListener = streamMessageListener;
    }

    @Override
    public void run(ApplicationArguments args) {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer = StreamMessageListenerContainer
                .create(this.redisConnectionFactory,
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                                .builder()
                                .batchSize(10)
                                .executor(this.threadPoolTaskExecutor)
                                .errorHandler(Throwable::printStackTrace)
                                .pollTimeout(Duration.ZERO)
                                .serializer(new StringRedisSerializer())
                                .build()
                );
        streamMessageListenerContainer.receive(Consumer.from("fpl", "consumer-1"), StreamOffset.create("Stream::EventAfterDeadline", ReadOffset.lastConsumed()), this.streamMessageListener);
//        streamMessageListenerContainer.receive(Consumer.from("fpl", "fpl-1"), StreamOffset.create("Stream::EventMatchDay", ReadOffset.lastConsumed()), this.streamMessageListener);
//        streamMessageListenerContainer.receive(Consumer.from("fpl", "fpl-1"), StreamOffset.create("Stream::EventMatch", ReadOffset.lastConsumed()), this.streamMessageListener);
        this.streamMessageListenerContainer = streamMessageListenerContainer;
        // start
        this.streamMessageListenerContainer.start();
    }

    @Override
    public void destroy() {
        this.streamMessageListenerContainer.stop();
    }

}
