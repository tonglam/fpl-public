package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/10
 */
@Data
@Accessors(chain = true)
public class TournamentGroupEventGroupFixtureData {

    private int groupId;
    private String groupName;
    private List<TournamentGroupEventEntryFixtureData> eventEntryFixtureList;

}
