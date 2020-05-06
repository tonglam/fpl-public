package com.tong.fpl.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by tong on 2018/7/10
 */
@Component
public class BatchUtils {

    /**
     * 分批list
     *
     * @param sourceList 要分批的list
     * @param batchCount 每批list的个数
     * @return List<List < ?>>                                                                                                                        <                                                                                                                               Object>>
     */
    public static List<List<?>> batchList(List<?> sourceList, int batchCount) {
        List<List<?>> returnList = new ArrayList<>();
        int startIndex = 0; // 从第0个下标开始
        while (startIndex < sourceList.size()) {
            int endIndex;
            if (sourceList.size() - batchCount < startIndex) {
                endIndex = sourceList.size();
            } else {
                endIndex = startIndex + batchCount;
            }
            returnList.add(sourceList.subList(startIndex, endIndex));
            startIndex = startIndex + batchCount; // 下一批
        }
        return returnList;
    }

}
