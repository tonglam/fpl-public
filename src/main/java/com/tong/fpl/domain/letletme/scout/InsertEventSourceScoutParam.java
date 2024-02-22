package com.tong.fpl.domain.letletme.scout;

import com.tong.fpl.domain.letletme.global.MapData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by tong on 2022/08/09
 */
@Data
@Accessors(chain = true)
public class InsertEventSourceScoutParam {

    private int event;
    private String source;
    private List<MapData<Integer>> scoutDataList;

}
