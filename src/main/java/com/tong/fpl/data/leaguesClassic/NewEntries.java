package com.tong.fpl.data.leaguesClassic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class NewEntries {
    @JsonProperty("has_next")
    private boolean hasNext;
    private int page;
    private List<ClassicResult> results;
}
