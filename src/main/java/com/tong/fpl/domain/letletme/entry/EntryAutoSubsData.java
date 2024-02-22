package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/11/3
 */
@Data
@Accessors(chain = true)
public class EntryAutoSubsData {

    private int event;
    private int autoSubsPoints;
    private List<EntryEventAutoSubsData> elementList;

}
