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
import java.util.Map;

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
        String key = StringUtils.joinWith("::", RedisExpirationKey.EventAfterDeadline.name(), event);
        this.addStringRecord(key, Collections.singletonMap("action", "insert_entry_event_pick"));
        this.addStringRecord(key, Collections.singletonMap("action", "insert_entry_event_transfers"));
        this.addStringRecord(key, Collections.singletonMap("action", "insert_entry_event_cup_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "insert_league_event_report"));
    }

    private void matchDayStream(int event) {
        String key = StringUtils.joinWith("::", RedisExpirationKey.EventMatchDay.name(), event);
        this.addStringRecord(key, Collections.singletonMap("action", "update_event_live"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_enty_event_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_entry_event_transfers"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_entry_event_cup_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_points_race_group_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_battle_race_group_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_knockout_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_zj_phase_one_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_zj_phase_two_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_zj_pk_result"));
        this.addStringRecord(key, Collections.singletonMap("action", "update_zj_tournament_result"));
    }

    private void matchStream(int event) {
        String key = StringUtils.joinWith("::", RedisExpirationKey.EventMatch.name(), event);
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("name", "KevinBlandy")).withStreamKey(key);


    }

    private void addStringRecord(String key, Map<String, String> valueMap) {
        StringRecord stringRecord = StreamRecords.string(valueMap).withStreamKey(key);
        this.stringRedisTemplate.opsForStream().add(stringRecord);
    }

}
