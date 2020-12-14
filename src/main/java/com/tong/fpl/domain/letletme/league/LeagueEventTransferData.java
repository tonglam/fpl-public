package com.tong.fpl.domain.letletme.league;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class LeagueEventTransferData {

	private int transfer;
	private int transferCost;
	private int transferInValue;
	private int transferOutValue;
	private String earnings;
	private int mostTransferIn;
	private int mostTransferOut;
	private List<ElementEventResultData> transferElementList;

}
