package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/2
 */
@Data
@Accessors(chain = true)
public class EntryAgainstInfoData {

    EntryInfoData homeEntryInfoData;
    EntryInfoData awayEntryInfoData;

}
