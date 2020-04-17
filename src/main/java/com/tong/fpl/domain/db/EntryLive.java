package com.tong.fpl.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Create by tong on 2020/3/17
 */
@Data
@Document(collection = "entry_live")
public class EntryLive {
    @Id
    private String id;
    private int entry;
    private int event;
    private int elemnet;
    private int elementType;
    private int position;
    private int minutes;
    private int point;
    private boolean isCaptain;
    private boolean isViceCaptain;
}
