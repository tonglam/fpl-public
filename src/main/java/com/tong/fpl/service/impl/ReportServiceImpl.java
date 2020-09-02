package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryCaptainStatEntity;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.db.EntryCaptainStatService;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.TournamentEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportServiceImpl implements IReportService {

	private final IQuerySerivce querySerivce;
	private final IRedisCacheSerive redisCacheSerive;
	private final EntryInfoService entryInfoService;
	private final EntryEventResultService entryEventResultService;
	private final EntryCaptainStatService entryCaptainStatService;
	private final TournamentEntryService tournamentEntryService;

	@Override
	public void insertEntryCaptainStat(String season, int tournamentId) {
		MybatisPlusConfig.season.set(season);
		List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
				.eq(TournamentEntryEntity::getTournamentId, tournamentId))
				.stream()
				.map(TournamentEntryEntity::getEntry)
				.collect(Collectors.toList());
		entryList.forEach(entry -> this.initEntryEventCaptainStat(season, entry));
		MybatisPlusConfig.season.remove();

	}

	private void initEntryEventCaptainStat(String season, int entry) {
		List<EntryCaptainStatEntity> entryCaptainStatList = Lists.newArrayList();
		// entry_info
		EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
		if (entryInfoEntity == null) {
			return;
		}
		// entry_event_result
		List<EntryEventResultEntity> entryEventResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, entry)
				.gt(EntryEventResultEntity::getEventPoints, 0));
		if (CollectionUtils.isEmpty(entryEventResultList)) {
			return;
		}
		entryEventResultList.forEach(entryEventResult -> {
			List<Pick> captainPickList = this.querySerivce.qryPickListFromPicks(season, entryEventResult.getEventPicks())
					.stream()
					.filter(o -> o.isCaptain() || o.isViceCaptain())
					.collect(Collectors.toList());
			Pick captainPick = this.getRealCaptainPoints(captainPickList);
			if (captainPick == null) {
				return;
			}
			EntryCaptainStatEntity entryCaptainStatEntity = new EntryCaptainStatEntity();
			entryCaptainStatEntity.setEntry(entry)
					.setEvent(entryEventResult.getEvent())
					.setEntryName(entryInfoEntity.getEntryName())
					.setPlayerName(entryInfoEntity.getPlayerName())
					.setOverallPoints(entryInfoEntity.getOverallPoints())
					.setOverallRank(entryInfoEntity.getOverallRank())
					.setChip(entryEventResult.getEventChip())
					.setElement(captainPick.getElement())
					.setWebName(this.redisCacheSerive.getPlayerByElememt(season, captainPick.getElement()).getWebName())
					.setPoints(captainPick.getPoints());
			entryCaptainStatEntity.setTotalPoints(Chip.getChipFromValue(entryCaptainStatEntity.getChip()).equals(Chip.TC) ?
					3 * entryCaptainStatEntity.getPoints() : 2 * entryCaptainStatEntity.getPoints());
			entryCaptainStatList.add(entryCaptainStatEntity);
		});
		// insert
		this.entryCaptainStatService.saveBatch(entryCaptainStatList);
		log.info("insert entry_captain_stat size is " + entryCaptainStatList.size() + "!");
	}

	private Pick getRealCaptainPoints(List<Pick> captainPickList) {
		if (CollectionUtils.isEmpty(captainPickList)) {
			return null;
		}
		Pick captain = captainPickList.stream().filter(Pick::isCaptain).findFirst().orElse(null);
		Pick viceCaptain = captainPickList.stream().filter(Pick::isViceCaptain).findFirst().orElse(null);
		if (captain == null || viceCaptain == null) {
			return null;
		}
		if (captain.getPoints() == 0 && viceCaptain.getPoints() > 0) {
			return viceCaptain;
		}
		return captain;
	}

}
