package com.tong.fpl;

import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.GwEntry;
import com.tong.fpl.data.response.LeagueClassicRes;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class ServiceTest extends FplApplicationTests {

	@Autowired
	private StaticService staticService;

	@Autowired
	private InterfaceService interfaceService;

	@Autowired
	private WeekPointsService weekPointsService;

	@Autowired
	private CalcLivePointsService calcLivePointsService;

	@Autowired
	private CreateNewCupsService createNewCupsService;

	@Test
	public void insertEvents() {
		this.staticService.insertEvent();
	}

	@Test
	public void insertTeams() {
		this.staticService.insertTeam();
	}

	@Test
	public void insertPlayers() {
		this.staticService.insertPlayers();
	}

	@Test
	public void insertGwLive() {
		this.staticService.insertEventLive(39, Constant.PL_PROFILE);
	}

	@Test
	public void userHostory() {
		this.interfaceService.getUserHistory(3697, Constant.PL_PROFILE);
	}

	@Test
	public void classic() {
		Optional<LeagueClassicRes> leagueClassic = this.interfaceService.getLeaguesClassic(710, Constant.PL_PROFILE, 1);
		System.out.println("done!");
	}

	@Test
	public void weekPoint() {
		List<GwEntry> list = this.weekPointsService.calcWeekPoints(710, 39);
		System.out.println(list.toString());
	}

	@Test
	public void calcPoints() {
		Map<List<EntryLiveEntity>, Integer> resultMap = this.calcLivePointsService.calcLivePointsService(3697, 39, Constant.PL_PROFILE);
		System.out.println(resultMap.toString());
	}

	@Test
	public void createNewCup() {
		this.createNewCupsService.createNewCup("https://fantasy.premierleague.com/leagues/710/standings/c",
				"test", "tong",
				"31+", "32+",
				7, 4,
				true);
	}

}
