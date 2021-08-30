package com.tong.fpl.domain.letletme.entry;

import com.tong.fpl.domain.letletme.global.MapData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/14
 */
@Data
@Accessors(chain = true)
public class EntryHistoryData {

    private int entry;
    private List<EntryHistoryInfoData> historyList;
    private List<MapData<String>> chips;

}
