package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/1/20
 */
public class StaticTest extends FplApplicationTests {

	@Autowired
	private IInterfaceService interfaceService;

	@ParameterizedTest
	@CsvSource({"3697"})
	void userHostory(int entry) {
		this.interfaceService.getUserHistory(entry);
	}

}
