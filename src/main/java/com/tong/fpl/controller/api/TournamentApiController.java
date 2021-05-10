package com.tong.fpl.controller.api;

import com.tong.fpl.api.ITournamentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentApiController {

    private final ITournamentApi tournamentApi;

}
