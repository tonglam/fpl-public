package com.tong.fpl.domain.letletme.global;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/9
 */
@Data
@Accessors(chain = true)
public class DropdownData {

    private String txt;
    private String event;

}
