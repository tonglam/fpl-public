package com.tong.fpl.task;

import com.tong.fpl.constant.enums.ReportLeague;
import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Create by tong on 2020/9/16
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

	private final IReportService reportService;
	private final IQuerySerivce querySerivce;

	// should start after deadline
	@Scheduled(cron = "0 45 19 * * *")
	public void insertTeamSelectStat() {
		int event = this.querySerivce.getCurrentEvent();
		Arrays.stream(ReportLeague.values()).forEach(o -> {
			int leagueId = o.getId();
			try {
				TaskLog.info("start league:{}", leagueId);
				this.reportService.inertTeamSelectStat(event, o.getType(), leagueId, o.getLimit());
				TaskLog.info("end league:{}", o.getId());
			} catch (Exception e) {
				e.printStackTrace();
				TaskLog.error("league:{}, error:{}", leagueId, e.getMessage());
				this.insertLeagueResultStat();
			}
		});
	}

	@Scheduled(cron = "0 0 8 * * *")
	public void insertLeagueResultStat() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isLastMatchDay(event)) {
			return;
		}
		TaskLog.info("start true insertLeagueResultStat task");
		Arrays.stream(ReportLeague.values()).forEach(o -> {
			int leagueId = o.getId();
			try {
				TaskLog.info("start league:{}", leagueId);
				this.reportService.insertLeagueResultStat(event, o.getType(), leagueId, o.getLimit());
				TaskLog.info("end league:{}", leagueId);
			} catch (Exception e) {
				e.printStackTrace();
				TaskLog.error("league:{}, error:{}", leagueId, e.getMessage());
				this.insertLeagueResultStat();
			}
		});
	}

}
