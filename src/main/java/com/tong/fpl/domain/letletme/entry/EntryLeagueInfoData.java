package com.tong.fpl.domain.letletme.entry;

import com.tong.fpl.domain.data.entry.Classic;
import com.tong.fpl.domain.data.entry.Cup;
import com.tong.fpl.domain.data.entry.H2h;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/14
 */
@Data
@Accessors(chain = true)
public class EntryLeagueInfoData {

    private int entry;
    private int event;
    private List<Classic> classic;
    private List<H2h> h2h;
    private Cup cup;

}
