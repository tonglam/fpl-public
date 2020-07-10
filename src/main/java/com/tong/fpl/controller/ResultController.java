package com.tong.fpl.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.data.fpl.TournamentGroupData;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.TournamentGroupEntity;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.TournamentGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Controller
@RequestMapping(value = "/result")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResultController {

	private final TournamentGroupService cupGroupService;
	private final EntryInfoService entryInfoService;

	@RequestMapping(value = {"", "/"})
	public String resultController() {
		return "result";
	}

	@ResponseBody
	@GetMapping(value = {"/getCupGroupResult"})
	public List<TournamentGroupData> getCupGroupResult(@RequestBody int groupId) {
		List<TournamentGroupData> cupGroupDataList = Lists.newArrayList();
		List<TournamentGroupEntity> cupGroupEntityList = this.cupGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, 1).eq(TournamentGroupEntity::getGroupId, groupId));
		cupGroupEntityList.forEach(cupGroupEntity -> {
			EntryInfoEntity entryInfoEntity = new EntryInfoEntity();
			TournamentGroupData cupGroupData = new TournamentGroupData();
			cupGroupData.setCupName("");
			cupGroupData.setEntry(cupGroupEntity.getEntry());
			cupGroupData.setEntryName(entryInfoEntity.getEntryName());
			cupGroupData.setPlayerName(entryInfoEntity.getPlayerName());
			cupGroupDataList.add(cupGroupData);
		});
		return cupGroupDataList;
	}

}
