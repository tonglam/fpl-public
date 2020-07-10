package com.tong.fpl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.fpl.TournamentCreateData;
import com.tong.fpl.domain.data.response.LeagueClassicRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.userHistory.Current;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.service.ITournamentManagementService;
import com.tong.fpl.service.IUpdateGwResultService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.impl.InterfaceServiceImpl;
import com.tong.fpl.service.impl.StaticServiceImpl;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/1/20
 */
public class ServiceTest extends FplApplicationTests {

	@Autowired
	private StaticServiceImpl staticService;

	@Autowired
	private InterfaceServiceImpl interfaceService;

	@Autowired
	private ITournamentManagementService tournamentManagement;

	@Autowired
	private IUpdateGwResultService updateGwResultService;

	@Autowired
	private EntryInfoService entryInfoService;

	@Test
	public void insertEvents() {
		this.staticService.insertEvent();
	}

	@Test
	public void insertPlayers() {
		this.staticService.insertPlayers();
	}

	@Test
	public void insertPlayerValue() {
		this.staticService.insertPlayerValue();
	}

	@Test
	public void insertGwFixture() {
		this.staticService.insertEventFixture(46);
	}

	@Test
	public void insertGwLive() {
		this.staticService.insertEventLive(42);
	}

	@Test
	public void userHostory() {
		this.interfaceService.getUserHistory(3697);
	}

	@Test
	public void classic() {
		Optional<LeagueClassicRes> leagueClassic = this.interfaceService.getLeaguesClassic(710, 1);
		System.out.println("done!");
	}

	@Test
	public void createNewCup() {
		try {
			TournamentCreateData tournamentCreateData = new TournamentCreateData();
			tournamentCreateData.setUrl("https://fantasy.premierleague.com/leagues/710/standings/c");
			tournamentCreateData.setTournamentName("letletme");
			tournamentCreateData.setCreator("tong");
			tournamentCreateData.setGroupMode("No_group");
			tournamentCreateData.setKnockoutMode("Single_round");
			tournamentCreateData.setKnockoutStartGw("1");
			String result = this.tournamentManagement.createNewTournament(tournamentCreateData);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void saveEntryInfo() {
		this.tournamentManagement.saveTournamentEntryInfo(1, LeagueType.Classic.name(), 710);
		System.out.println(1);
	}

	@Test
	public void drawKnockouts() {
		this.tournamentManagement.drawKnockouts(1, GroupMode.No_group.name(), 1, 2,
				2, 107, 1, 7);
		System.out.println(1);
	}

	@Test
	public void drawGroupBattle() {
		this.tournamentManagement.drawGroupBattle(1, GroupMode.Battle_race.name(), 1, 16, 8);
	}

	@Test
	public void chinaUsers() {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, 3212061)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			UserHistoryRes historyRes = mapper.readValue(result, UserHistoryRes.class);
			int transfer = getTransferNum(historyRes);
			int cost = getTransferCost(historyRes);
			System.out.println(CommonUtils.checkActive(43, historyRes));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(1);
	}

	private int getTransferCost(UserHistoryRes historyRes) {
		return historyRes.getCurrent().stream().map(Current::getEventTransfersCost).reduce(0, (sum, i) -> sum += i);
	}

	private int getTransferNum(UserHistoryRes historyRes) {
		return historyRes.getCurrent().stream().map(Current::getEventTransfers).reduce(0, (sum, i) -> sum += i);
	}

	@Test
	public void updateEventResult() {
		List<Integer> entryList = this.entryInfoService.list().stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		IntStream.range(2, 44).forEach(event -> {
			System.out.println("start event: " + event);
			this.updateGwResultService.upsertEventResult(event, entryList);
		});
	}

}
