package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/31
 */
@Data
@Accessors(chain = true)
public class EntryAutoSubData {

	private int elementIn;
	private int elementInType;
	private String elementInTypeName;
	private String elementInWebName;
	private int elementInPoints;
	private int elementInTeamId;
	private String elementInTeamName;
	private String elementInTeamShortName;
	private int elementOut;
	private int elementOutType;
	private String elementOutTypeName;
	private String elementOutWebName;
	private int elementOutPoints;
	private int elementOutTeamId;
	private String elementOutTeamName;
	private String elementOutTeamShortName;

}
