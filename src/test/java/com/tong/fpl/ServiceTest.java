package com.tong.fpl;

import com.tong.fpl.constant.GroupModeEnum;
import com.tong.fpl.constant.LeagueTypeEnum;
import com.tong.fpl.domain.data.fpl.TournamentCreateData;
import com.tong.fpl.domain.data.response.FixturesRes;
import com.tong.fpl.domain.data.response.LeagueClassicRes;
import com.tong.fpl.service.impl.InterfaceServiceImpl;
import com.tong.fpl.service.impl.StaticServiceImpl;
import com.tong.fpl.service.impl.TournamentManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class ServiceTest extends FplApplicationTests {

	@Autowired
	private StaticServiceImpl staticService;

	@Autowired
	private InterfaceServiceImpl interfaceService;

	@Autowired
	private TournamentManagementServiceImpl tournamentManagement;

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
		this.staticService.insertEventLive(39);
	}

	@Test
	public void userHostory() {
		this.interfaceService.getUserHistory(3697);
	}

	@Test
	public void fixtures() {
		Optional<List<FixturesRes>> a = this.interfaceService.getFixturesInfo(41);
		a.ifPresent(System.out::println);
		System.out.println(1);
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
		this.tournamentManagement.saveTournamentEntryInfo(1, LeagueTypeEnum.Classic.name(), 710);
		System.out.println(1);
	}

	@Test
	public void drawKnockouts() {
		this.tournamentManagement.drawKnockouts(1, GroupModeEnum.No_group.name(), 1, 2,
				2, 107, 1, 7);
		System.out.println(1);
	}

	@Test
	public void drawGroupBattle() {
		this.tournamentManagement.drawGroupBattle(1, GroupModeEnum.Battle_race.name(), 1, 16, 8);
	}

}
