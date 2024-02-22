package com.tong.fpl.domain.special;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by tong on 2022/02/25
 */
@Data
@Accessors(chain = true)
public class GroupInfoData {

    private int groupId;
    private int captainId;
    private List<EntryInfoData> entryList;

}
