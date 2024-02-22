package com.tong.fpl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

/**
 * Create by tong on 2020/8/3
 */
@SpringBootTest()
@AutoConfigureMockMvc
public class RESTfulTest {

    @Autowired
    private MockMvc mockMvc;

    MvcResult mockResult(String url, MultiValueMap<String, String> params) throws Exception {
        return this.mockMvc
                .perform(MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.ALL).params(params))
                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @ParameterizedTest
    @CsvSource({"/api/qryEntryEventResult, 1920, 1, 1404"})
    void qryEntryEventResult(String url, String season, String event, String entry) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("season", season);
        params.add("event", event);
        params.add("entry", entry);
        MvcResult mvcResult = this.mockResult(url, params);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/api/qryDayChangePlayerValue, 20200726"})
    void qryDayChangePlayerValue(String url, String changeDate) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("changeDate", changeDate);
        MvcResult mvcResult = this.mockResult(url, params);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/my_fpl/qryPlayerDataList, 1, 20"})
    void qryPlayerDataList(String url, String current, String size) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("current", current);
        params.add("size", size);
        MvcResult mvcResult = this.mockResult(url, params);
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/live/calcLivePointsByEntry"})
    void qryEntryLivePoints(String url) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        MvcResult mvcResult = this.mockResult(url, params);
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/live/calcLivePointsByTournament, 2"})
    void qryTournamentLivePoints(String url, String tournamentId) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("tournamentId", tournamentId);
        MvcResult mvcResult = this.mockResult(url, params);
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/api/group/updateEventScoutResult, 36"})
    void updateEventScoutResult(String url, String event) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("event", event);
        MvcResult mvcResult = this.mockResult(url, params);
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/api/stat/refreshPlayerValue"})
    void refreshPlayerValue(String url) throws Exception {
        long start = System.currentTimeMillis();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        MvcResult mvcResult = this.mockResult(url, params);
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        long end = System.currentTimeMillis();
        System.out.println("escape: " + ((end - start) / 1000) + "s!");
        System.out.println(1);
    }

}
