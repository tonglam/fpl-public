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
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer, StringRedisTemplate stringRedisTemplate) {
        super(listenerContainer);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        super.onMessage(message, pattern);
        String event = StringUtils.substringAfter(message.toString(), "::");
        RedisExpirationKey redisExpirationKey = RedisExpirationKey.getExpirationKey(StringUtils.substringBefore(message.toString(), "::"));
        if (redisExpirationKey == null) {
            return;
        }
        String key = StringUtils.joinWith("::", "Stream", redisExpirationKey.name());
        switch (redisExpirationKey) {
            case EventAfterDeadline: {
                this.afterDeadlineStream(key, event);
                break;
            }
            case EventMatchDay: {
                this.matchDayStream(key, event);
                break;
            }
            case EventMatch: {
                this.matchStream(key);
                break;
            }
        }
    }

    private void afterDeadlineStream(String key, String event) {
        this.addStringRecord(key, Collections.singletonMap("insertTournamentEntryEventTransfers", event));
        this.addStringRecord(key, Collections.singletonMap("insertTournamentEntryEventCupResult", event));
    }

    private void matchDayStream(String key, String event) {
        this.addStringRecord(key, Collections.singletonMap("updateEventLiveData", event));
        this.addStringRecord(key, Collections.singletonMap("updateTournamentEventTransfers", event));
        this.addStringRecord(key, Collections.singletonMap("upsertTournamentEntryEventCupResult", event));
        this.addStringRecord(key, Collections.singletonMap("updatePointsRaceGroupResult", event));
        this.addStringRecord(key, Collections.singletonMap("updateBattleRaceGroupResult", event));
        this.addStringRecord(key, Collections.singletonMap("updateKnockoutResult", event));
        this.addStringRecord(key, Collections.singletonMap("updateZjPhaseOneResult", event));
        this.addStringRecord(key, Collections.singletonMap("updateZjPhaseTwoResult", event));
        this.addStringRecord(key, Collections.singletonMap("updateZjPkResult", event));
        this.addStringRecord(key, Collections.singletonMap("updateZjTournamentResult", event));
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
