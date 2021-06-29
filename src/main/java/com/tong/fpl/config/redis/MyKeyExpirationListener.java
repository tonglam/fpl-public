package com.tong.fpl.config.redis;

import com.tong.fpl.constant.enums.RedisExpirationKey;
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
import java.util.Map;

/**
 * Create by tong on 2021/4/12
 */
@Component
public class MyKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final StringRedisTemplate stringRedisTemplate;

    public MyKeyExpirationListener(RedisMessageListenerContainer listenerContainer, StringRedisTemplate stringRedisTemplate) {
        super(listenerContainer);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        super.onMessage(message, pattern);
        RedisExpirationKey redisExpirationKey = RedisExpirationKey.getExpirationKey(StringUtils.substringBefore(message.toString(), "::"));
        if (redisExpirationKey == null) {
            return;
        }
        String key = StringUtils.joinWith("::", "Stream", redisExpirationKey.name());
        switch (redisExpirationKey) {
            case EventAfterDeadline: {
                this.afterDeadlineStream(key);
                break;
            }
            case EventMatchDay: {
                this.matchDayStream(key);
                break;
            }
            case EventMatch: {
                this.matchStream(key);
                break;
            }
        }
    }

    private void afterDeadlineStream(String key) {
        this.addStringRecord(key, Collections.singletonMap("entry_event_pick", "insert"));
        this.addStringRecord(key, Collections.singletonMap("entry_event_transfers", "insert"));
        this.addStringRecord(key, Collections.singletonMap("entry_event_cup_result", "insert"));
        this.addStringRecord(key, Collections.singletonMap("league_event_report", "insert"));
    }

    private void matchDayStream(String key) {
        this.addStringRecord(key, Collections.singletonMap("event_live", "update"));
        this.addStringRecord(key, Collections.singletonMap("entry_event_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("entry_event_transfers", "update"));
        this.addStringRecord(key, Collections.singletonMap("entry_event_cup_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("points_race_group_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("battle_race_group_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("knockout_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("zj_phase_one_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("zj_phase_two_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("zj_pk_result", "update"));
        this.addStringRecord(key, Collections.singletonMap("zj_tournament_result", "update"));
    }

    private void matchStream(String key) {
        this.addStringRecord(key, Collections.singletonMap("start", "true"));
        this.addStringRecord(key, Collections.singletonMap("end", "false"));
    }

    private void addStringRecord(String key, Map<String, String> valueMap) {
        StringRecord stringRecord = StreamRecords.string(valueMap).withStreamKey(key);
        this.stringRedisTemplate.opsForStream().add(stringRecord);
    }

}
