package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/29
 */
@Data
@Accessors(chain = true)
public class ZjTournamentCreateData {

    private String name;
    private int adminerEntry;
    private String creator;
    private List<ZjTournamentGroupData> groupList;

}
