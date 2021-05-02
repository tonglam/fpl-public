package com.tong.fpl.domain.letletme.lottery;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/2
 */
@Data
@Accessors(chain = true)
public class LotteryData {

    private int id;
    private String name;
    private String creator;
    private Map<String, Integer> prizeMap; // name -> number
    private List<Integer> entryList;
    private Map<String, Integer> resultMap; // prize -> entry
    private String createTime;

}
