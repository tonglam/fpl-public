package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * Create by tong on 2020/7/13
 */
@Data
@Accessors(chain = true)
public class ElementLiveData {

	private int event;
	private int element;
	private String webName;
	private int elementType;
	private int position;
	private int multiplier;
	private boolean isCaptain;
	private boolean isViceCaptain;
	private boolean isGwStarted;
	private boolean isPlayed;
	private int minutes;
	private int goalsScored;
	private int assists;
	private int cleanSheets;
	private int goalsConceded;
	private int ownGoals;
	private int penaltiesSaved;
	private int penaltiesMissed;
	private int yellowCards;
	private int redCards;
	private int saves;
	private int bonus;
	private int bps;
	private int totalPoints;
	private boolean pickAvtive;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ElementLiveData that = (ElementLiveData) o;
		return event == that.event &&
				element == that.element;
	}

	@Override
	public int hashCode() {
		return Objects.hash(event, element);
	}

}
