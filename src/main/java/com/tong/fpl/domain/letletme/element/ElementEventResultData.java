package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * Create by tong on 2020/7/13
 */
@Data
@Accessors(chain = true)
public class ElementEventResultData {

    private String season;
    private int event;
    private int element;
    private int code;
    private String webName;
    private double price;
    private int elementType;
    private String elementTypeName;
    private int teamId;
    private int teamCode;
    private String teamName;
    private String teamShortName;
    private int againstId;
    private String againstName;
    private String againstShortName;
    private String wasHome;
    private String score;
    private int position;
    private int multiplier;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private boolean isGwStarted;
    private boolean isGwFinished;
    private boolean isPlayed;
    private int playStatus;
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
    private boolean pickActive;
    private boolean autoSub;
    private boolean bgw;
    private boolean dgw;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ElementEventResultData that = (ElementEventResultData) o;
        return this.event == that.event &&
                this.element == that.element;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.event, this.element);
    }

}
