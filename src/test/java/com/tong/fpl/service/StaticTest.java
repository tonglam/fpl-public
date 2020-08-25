package com.tong.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.response.LeagueClassicRes;
import com.tong.fpl.domain.data.response.StaticRes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class StaticTest extends FplApplicationTests {

	@Autowired
	private IStaticSerive staticService;
	@Autowired
	private IInterfaceService interfaceService;

	@Test
	void insertPlayerValue() {
		this.staticService.insertPlayerValue();
	}

	@Test
	void updatePlayerValue() {
		this.staticService.updatePlayerValue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"E:\\0719.json"})
	void insertPlayerValueFromFile(String filename) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			StaticRes staticRes = mapper.readValue(new File(filename), StaticRes.class);
			this.staticService.insertPlayerValueEntity(staticRes);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		Optional<LeagueClassicRes> leagueClassic = this.interfaceService.getLeaguesClassic(710, 1);
		System.out.println("done!");
	}


}
