package com.tong.fpl.domain.db;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("element_type")
    private int elementType;
    private int position;
    private int minutes;
    private int point;
    @JsonProperty("is_captain")
    private boolean isCaptain;
    @JsonProperty("is_vice_captain")
    private boolean isViceCaptain;
}
