package com.tong.fpl.domain.letletme.global;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/5
 */
@Data
@Accessors(chain = true)
public class BracketData {

    private List<List<String>> teams;
    private List<List<String>> result;

}
