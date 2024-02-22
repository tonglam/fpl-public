package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Create by tong on 2020/9/18
 */
@Data
@Accessors(chain = true)
public class LeagueStatData {

    private int event;
    private String name;
    private LinkedHashMap<String, String> captainSelectedMap;
    private LinkedHashMap<String, String> captainSelectedEoMap;
    private LinkedHashMap<String, String> viceCaptainSelectedMap;
    private LinkedHashMap<String, String> mostTransferIn;
    private LinkedHashMap<String, String> mostTransferOut;
    private LinkedHashMap<String, String> topSelectedPlayerMap;
    private LinkedHashMap<String, String> topSelectedPlayerEoMap;
    private LinkedHashMap<Integer, Map<String, String>> topSelectedTeamMap;

}
