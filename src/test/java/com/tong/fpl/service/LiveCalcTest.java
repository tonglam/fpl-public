package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.letletme.LiveCalaData;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.service.db.PlayerService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Create by tong on 2020/7/15
 */
public class LiveCalcTest extends FplApplicationTests {

	@Autowired
	private ILiveCalcService liveCalcService;
	@Autowired
	private PlayerService playerService;


	@ParameterizedTest
	@CsvSource({"45, 3697"})
	void calcLivePoints(int event, int entry) {
		LiveCalaData liveCalaData = this.liveCalcService.calcLivePointsByEntry(event, entry);
		System.out.println("points: " + liveCalaData.getLivePoints());
		liveCalaData.getPickList().forEach(o -> {
			String webName = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda()
					.eq(PlayerEntity::getElement, o.getElement())).getWebName();
			System.out.println(webName);
		});
	}

	@ParameterizedTest
	@CsvSource({"45"})
	void calcLivePointsByElementList(int event) {
		Map<Integer, Integer> map = Maps.newHashMap();
		map.put(1, 93);
		map.put(2, 122);
		map.put(3, 433);
		map.put(4, 141);
		map.put(5, 389);
		map.put(6, 214);
		map.put(7, 431);
		map.put(8, 191);
		map.put(9, 618);
		map.put(10, 234);
		map.put(11, 211);
		LiveCalaData liveCalaData = this.liveCalcService.calcLivePointsByElementList(event, map, 214, 191);
		System.out.println("points: " + liveCalaData.getLivePoints());
		liveCalaData.getPickList().forEach(o -> {
			String webName = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda()
					.eq(PlayerEntity::getElement, o.getElement())).getWebName();
			System.out.println(webName);
		});
	}

}
