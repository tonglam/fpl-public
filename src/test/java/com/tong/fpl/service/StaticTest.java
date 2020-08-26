package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.response.LeagueClassicRes;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.service.db.EntryInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class StaticTest extends FplApplicationTests {

	@Autowired
	private IStaticSerive staticService;
	@Autowired
	private IInterfaceService interfaceService;
	@Autowired
	private EntryInfoService entryInfoService;

	@Test
	void insertPlayerValue() {
		this.staticService.insertPlayerValue();
	}

	@Test
	void updatePlayerValue() {
		this.staticService.updatePlayerValue();
	}

	@ParameterizedTest
	@CsvSource({"46"})
	void insertGwLive(int event) {
		this.staticService.insertEventLive(event);
	}

	@ParameterizedTest
	@CsvSource({"3697"})
	void userHostory(int entry) {
		this.interfaceService.getUserHistory(entry);
	}

	@Test
	void classic() {
		Optional<LeagueClassicRes> resResult = this.interfaceService.getLeaguesClassic(11316, 1);
		if (resResult.isPresent()) {
			LeagueClassicRes leagueClassicRes = resResult.get();
			if (!CollectionUtils.isEmpty(leagueClassicRes.getNewEntries().getResults())) {
				leagueClassicRes.getNewEntries().getResults().forEach(o -> {
					if (o.getEntry() != 1870) {
						return;
					}
					EntryInfoEntity entryInfoEntity = new EntryInfoEntity();
					entryInfoEntity.setEntry(o.getEntry());
					entryInfoEntity.setEntryName(o.getEntryName());
					entryInfoEntity.setPlayerName(o.getPlayerName());
					this.entryInfoService.save(entryInfoEntity);
				});
			}
		}
		System.out.println("done!");
	}

}
