package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/10/22
 */
@Data
@Accessors(chain = true)
public class EntryResultData {

    private EntryInfoData entryInfoData;
    private List<EntryEventResultData> eventResultDataList;

}
