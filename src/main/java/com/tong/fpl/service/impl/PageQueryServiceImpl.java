package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.data.letletme.table.PlayTableData;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.service.IPageQueryService;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PageQueryServiceImpl implements IPageQueryService {

	private final IQuerySerivce querySerivce;
	private final PlayerService playerService;

	@Cacheable(value = "pagePlayerDataList", key = "#current+'::'+#size", unless = "#result.records.size() eq 0")
	@Override
	public Page<PlayTableData> qryPagePlayerDataList(long current, long size) {
		List<PlayTableData> list = Lists.newArrayList();
		boolean searchTotal = false;
		if (current == 1) { // 第一页计算总数
			searchTotal = true;
		}
		Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
				new Page<>(current, size, searchTotal), new QueryWrapper<>());
		playerPage.getRecords().forEach(o -> list.add(BeanUtil.copyProperties(this.querySerivce.initPlayerInfo(o), PlayTableData.class)));
		Page<PlayTableData> page = new Page<>(current, size, playerPage.getTotal());
		page.setRecords(list);
		return page;
	}

}
