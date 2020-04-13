package com.tong.fpl.domain.data.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.leaguesClassic.ClassicResult;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class Standings {
    @JsonProperty("has_next")
    private boolean hasNext;
    private int page;
    private List<ClassicResult> results;
}
