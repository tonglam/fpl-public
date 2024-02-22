package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/14
 */
@Data
@Accessors(chain = true)
public class EntryQueryParam {

    private String entryName;
    private String playerName;

}
