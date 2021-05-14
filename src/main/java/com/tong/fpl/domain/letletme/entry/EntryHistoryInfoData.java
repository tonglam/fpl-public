package com.tong.fpl.domain.letletme.entry;

import com.tong.fpl.domain.data.userHistory.HistoryChips;
import com.tong.fpl.domain.data.userHistory.Past;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/14
 */
@Data
@Accessors(chain = true)
public class EntryHistoryInfoData {

    private int entry;
    private List<Past> past;
    private List<HistoryChips> chips;

}
