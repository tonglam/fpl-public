package com.tong.fpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.db.FACup;
import com.tong.fpl.domain.response.FaCupRes;
import com.tong.fpl.domain.response.StaticRes;
import com.tong.fpl.utils.BatchUtils;
import com.tong.fpl.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Create by tong on 2020/1/19
 */
public class CommonTest extends FplApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<FACup> insertList = Lists.newArrayList();

    @Test
    public void http() {
        try {
            String result = HttpUtils.httpGet("chrome://settings/cookies/detail?site=fantasy.premierleague.com");
            System.out.println("result");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void json() {
        try {
            String result = HttpUtils.httpGet("https://fantasy.premierleague.com/api/bootstrap-static/");
            if (StringUtils.isEmpty(result)) {
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            StaticRes staticRes = mapper.readValue(result, StaticRes.class);
            System.out.println("result");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void login() {
        try {
            HttpUtils.httpLogin("bluedragon00000@sina.com", "9111130609fpl");
            String result = HttpUtils.httpGet("https://fantasy.premierleague.com/api/entry/378912/");
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fa() throws JsonProcessingException {
        String fileName = "E:\\home\\data.json";
        // 打开文件
        File file = new File(fileName);
        if (!file.exists()) {
            throw new RuntimeException("input file does not exists : " + fileName);
        }
        List<String> log = Lists.newArrayList();
        // 获取日志内容
        try {
            log = Files.readLines(file, Charsets.UTF_8);
        } catch (IOException e) {
        }
        for (String line :
                log) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            FaCupRes faCup = mapper.readValue(line, FaCupRes.class);
            FACup faCupTable1 = new FACup();
            faCupTable1.setEntry(faCup.getEntryIds().get(0));
            faCupTable1.setGroup_id(faCup.getId());
            faCupTable1.setTeam_name(faCup.getEntryNames().get(0));
            faCupTable1.setPlayer_name(faCup.getPlayerNames().get(0));
            faCupTable1.setRound_1_event(29);
            insertList.add(faCupTable1);
            FACup faCupTable2 = new FACup();
            faCupTable2.setEntry(faCup.getEntryIds().get(1));
            faCupTable2.setGroup_id(faCup.getId());
            faCupTable2.setTeam_name(faCup.getEntryNames().get(1));
            faCupTable2.setPlayer_name(faCup.getPlayerNames().get(1));
            faCupTable2.setRound_1_event(29);
            insertList.add(faCupTable2);
        }
        List<List<?>> eventsAll = BatchUtils.batchList(insertList, Constant.BATCH_COUNT);
        for (List list : eventsAll
        ) {
            BatchUtils.batchInsetMongo(list, "FA_cup");
        }

    }

}
