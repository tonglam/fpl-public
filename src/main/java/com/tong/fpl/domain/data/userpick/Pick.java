package com.tong.fpl.domain.data.userpick;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/3/11
 */
@Data
public class Pick implements Comparable<Pick> {
    private Integer element;
    private Integer position;
    private Integer multiplier;
    @JsonProperty("is_captain")
    private boolean isCaptain;
    @JsonProperty("is_vice_captain")
    private boolean isViceCaptain;
	private int points;

    @Override
    public int compareTo(Pick pick) {
        return this.getElement().compareTo(pick.getElement());
    }
}
