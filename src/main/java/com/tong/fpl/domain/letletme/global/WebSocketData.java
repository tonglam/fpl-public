package com.tong.fpl.domain.letletme.global;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/22
 */
@Data
@Accessors(chain = true)
public class WebSocketData {

	private String from;
	private String content;
	private String targetUser;
	private String destination;

}
