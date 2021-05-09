package com.tong.fpl.controller.api;

import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Create by tong on 2021/2/26
 */
@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiLiveController {

    private final IQueryService queryService;

    @RequestMapping("/qryLiveTeamData")
    @ResponseBody
    public List<LiveMatchTeamData> qryLiveTeamDataList(@RequestParam int statusId) {
        return this.queryService.qryLiveTeamDataList(statusId);
    }

}
