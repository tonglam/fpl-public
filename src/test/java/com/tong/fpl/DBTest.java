package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.PlayerStatService;
import com.tong.fpl.service.db.TournamentKnockoutResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

	@Autowired
	private EntryInfoService entryInfoService;
	@Autowired
	private TournamentKnockoutResultService tournamentKnockoutResultService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private PlayerStatService playerStatService;


	@Test
	void test() {
		long startTime = System.currentTimeMillis();
		List<PlayerEntity> list = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getElement, 211));
		long endTime = System.currentTimeMillis();
		System.out.println("escape: " + (endTime - startTime) + "ms");
		System.out.println(1);
	}

	@Test
	void page() {
		Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
				new Page<>(2, 20, false), new QueryWrapper<>());
		System.out.println(1);
	}

	@Test
	void dynamic() {
		MybatisPlusConfig.season.set("1920");
		List<EntryInfoEntity> entryInfoEntity = this.entryInfoService.list();
		System.out.println(1);
	}

}
