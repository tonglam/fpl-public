package com.tong.fpl.domain.letletme.live;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/10/30
 */
@Data
@Accessors(chain = true)
public class LiveMatchTeamData {

	List<ElementEventResultData> elementEventResultList;
	private int teamId;

}
