package com.tong.fpl.domain.data.letletme.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlayerQueryParam {

    private int element;
    private int code;
    @JsonProperty("web_name")
    private String webName;

}
