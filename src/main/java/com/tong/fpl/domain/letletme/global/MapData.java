package com.tong.fpl.domain.letletme.global;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/18
 */
@Data
@Accessors(chain = true)
public class MapData<T> {

    private String key;
    private T value;

}
