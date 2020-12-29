package com.tong.fpl.task;

import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/9/16
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

	private final IQueryService queryService;
	private final IReportService reportService;

	@Scheduled(cron = "0 0/5 0-4,18-23 * * *")
	public void insertLeagueEventSelectStat() {
		int event = this.queryService.getCurrentEvent();
		if (!this.queryService.isSelectTime(event)) {
			return;
		}
		this.queryService.qryLeagueMap(event).forEach((leagueIdStr, leagueType) -> {
			int leagueId = Integer.parseInt(leagueIdStr);
			if (this.reportService.evenLeagueEventExists(event, leagueId, leagueType)) {
				return;
			}
			this.reportService.insertLeagueEventSelect(event, leagueId, leagueType, 0);
		});
	}

	@Scheduled(cron = "0 0 9,12 * * *")
	public void updateLeagueEventResult() {
		int event = this.queryService.getCurrentEvent();
		if (!this.queryService.isMatchDay(event)) {
			return;
		}
		this.queryService.qryLeagueMap(event).forEach((leagueIdStr, leagueType) ->
				this.reportService.updateLeagueEventResult(event, Integer.parseInt(leagueIdStr), leagueType));
	}

}
