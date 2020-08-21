package com.tong.fpl.domain.data.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlayerQueryParam {

    private int element;
    private int code;
    private String webName;

}
