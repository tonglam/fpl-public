package com.tong.fpl.mq;

import com.tong.fpl.constant.enums.RedisExpirationKey;
import com.tong.fpl.service.IQueryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Create by tong on 2021/4/12
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final IQueryService queryService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer, StringRedisTemplate stringRedisTemplate, IQueryService queryService) {
        super(listenerContainer);
        this.stringRedisTemplate = stringRedisTemplate;
        this.queryService = queryService;
    }

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        super.onMessage(message, pattern);
        RedisExpirationKey redisExpirationKey = RedisExpirationKey.getExpirationKey(StringUtils.substringBefore(message.toString(), "::"));
        if (redisExpirationKey == null) {
            return;
        }
        int event = this.queryService.getCurrentEvent();
        switch (redisExpirationKey) {
            case EventAfterDeadline: {
                this.afterDeadlineStream(event);
                break;
            }
            case EventMatchDay: {
                this.matchDayStream(event);
                break;
            }
            case EventMatch: {
                this.matchStream(event);
                break;
            }
        }
    }

    private void afterDeadlineStream(int event) {
        String key = StringUtils.joinWith("::", "Afterdeadline", event);
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("action", "KevinBlandy")).withStreamKey(key);
        this.stringRedisTemplate.opsForStream().add(stringRecord);
    }

    private void matchDayStream(int event) {
        String key = StringUtils.joinWith("::", "MatchDay", event);
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("name", "KevinBlandy")).withStreamKey(key);


    }

    private void matchStream(int event) {
        String key = StringUtils.joinWith("::", "Match", event);
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("name", "KevinBlandy")).withStreamKey(key);


    }

}
