package com.tong.fpl.domain.letletme.team;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/10/21
 */
@Data
@Accessors(chain = true)
public class TeamAgainstSeasonInfoData {

    private String season;
    List<TeamAgainstRecordData> seasonDataList;

}
