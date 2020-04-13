package com.tong.fpl.utils;

import com.tong.fpl.domain.db.FACup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by tong on 2018/7/10
 */
@Component
public class BatchUtils {

    private static MongoTemplate mongoTemplate;

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

    public static <T> void batchInsetMongo(List<? extends T> batchToSave, String collectionName) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collectionName);
        for (T o : batchToSave
        ) {
            bulkOperations.insert(o);
        }
        bulkOperations.execute();
    }

    public static void batchUpdateFACup(List<FACup> faCupTableList) {
        BulkOperations bulkOperations = BatchUtils.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "FA_cup");
        for (FACup faCupTable : faCupTableList) {
            Query query = new Query(Criteria.where("entry").is(faCupTable.getEntry()));
            Update update = new Update();
            if (faCupTable.getRound_1_event() > 0) {
                update.set("round_1_event", faCupTable.getRound_1_event());
            }
            if (faCupTable.getRound_2_event() > 0) {
                update.set("round_2_event", faCupTable.getRound_2_event());
            }
            update.set("round_1_point", faCupTable.getRound_1_point());
            update.set("round_2_point", faCupTable.getRound_2_point());
            if (faCupTable.getRound_1_cost() >= 0) {
                update.set("round_1_cost", faCupTable.getRound_1_cost());
            }
            if (faCupTable.getRound_2_cost() >= 0) {
                update.set("round_2_cost", faCupTable.getRound_2_cost());
            }
            update.set("round_1_winner", faCupTable.getRound_1_winner());

            update.set("round_2_winner", faCupTable.getRound_2_winner());
            update.set("winner", faCupTable.getWinner());
            if (StringUtils.isNotEmpty(faCupTable.getRound_1_chip())) {
                update.set("round_1_chip", faCupTable.getRound_1_chip());
            }
            if (StringUtils.isNotEmpty(faCupTable.getRound_2_chip())) {
                update.set("round_2_chip", faCupTable.getRound_2_chip());
            }
            if (faCupTable.isRound_1_update()) {
                update.set("round_1_update", true);
            }
            if (faCupTable.isRound_2_update()) {
                update.set("round_2_update", true);
            }
            if (faCupTable.getTotal_points() >= 0) {
                update.set("total_points", faCupTable.getTotal_points());
            }
            if (faCupTable.getOverall_rank() > 0) {
                update.set("overall_rank", faCupTable.getOverall_rank());
            }
            bulkOperations.updateOne(query, update);
        }
        bulkOperations.execute();
    }

    @Autowired
    private void setMongoTemplate(MongoTemplate mongoTemplate) {
        BatchUtils.mongoTemplate = mongoTemplate;
    }

}
