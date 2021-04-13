package com.tong.fpl.config.event;

import com.tong.fpl.constant.enums.RedisExpirationKey;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;

/**
 * Create by tong on 2021/4/12
 */
@Configuration
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

	public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
		super(listenerContainer);
	}

	@Override
	public void onMessage(Message message, @Nullable byte[] pattern) {
		super.onMessage(message, pattern);
		// listen special key expiration event
		if (!RedisExpirationKey.needListen(message.toString())) {
			return;
		}
		System.out.println(message.toString());
	}

}
