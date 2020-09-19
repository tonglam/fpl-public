package com.tong.fpl.domain.letletme.league;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/16
 */
@Data
@Accessors(chain = true)
public class LeagueInfoData {

    List<EntryInfoData> entryInfoList;
    private int id;
    private String type;
    private String name;
    private String created;
    private int adminEntry;
    private int startEvent;

}
