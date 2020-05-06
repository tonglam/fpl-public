package com.tong.fpl.data.response;

import com.tong.fpl.data.userHistory.Current;
import com.tong.fpl.data.userHistory.HistoryChips;
import com.tong.fpl.data.userHistory.Past;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class UserHistoryRes {
    private List<Current> current;
    private List<Past> past;
    private List<HistoryChips> chips;
}
