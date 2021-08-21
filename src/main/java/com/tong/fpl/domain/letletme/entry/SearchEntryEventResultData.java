package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/8/16
 */
@Data
@Accessors(chain = true)
public class SearchEntryEventResultData {

    private int element;
    private String webName;
    private int selectNum;
    private String selectByPercent;
    private List<EntryEventResultData> eventResultList;

}
