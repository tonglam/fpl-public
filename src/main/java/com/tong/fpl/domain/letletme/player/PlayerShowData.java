package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/12/8
 */
@Data
@Accessors(chain = true)
public class PlayerShowData {

	private int element;
	private int position;
	private int multiplier;
	private boolean isCaptain;
	private boolean isViceCaptain;
	private String webName;
	private int elementType;
	private String elementTypeName;
	private int teamId;
	private String teamName;
	private String teamShortName;
	private double price;
	private double sellPrice;
	private int totalPoints;
	private int chancePlayingNextRound;
	private int chancePlayingThisRound;
	private String news;
	private String selectedByPercent;
	private String pointsPerGame;
	private String form;
	private boolean inDreamteam;
	private boolean eventTransferIn;
	private boolean eventTransferOut;
	private List<PlayerShowFixtureData> fixtureList;

}
