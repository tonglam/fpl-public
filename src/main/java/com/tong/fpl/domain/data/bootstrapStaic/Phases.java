package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Create by tong on 2020/1/20
 */
@Data
@Document(collection = "phases")
public class Phases {
    private int id;
    private String name;
    @JsonProperty("start_event")
    private int startEvent;
    @JsonProperty("stop_event")
    private int stopEvent;
}
