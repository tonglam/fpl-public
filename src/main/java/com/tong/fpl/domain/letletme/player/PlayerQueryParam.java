package com.tong.fpl.domain.letletme.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlayerQueryParam {

    private int element;
    private int code;
    @JsonProperty("web_name")
    private String webName;

}
