package com.tong.fpl.task;

import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IQuerySerivce;
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

	private final IReportService reportService;
	private final IQuerySerivce querySerivce;

	// should start after deadline
	@Scheduled(cron = "0 30 17 * * *")
	public void insertLeagueEventSelectStat() {
		try {
			TaskLog.info("start league:{}", 65);
			this.reportService.insertLeagueEventSelectStat(1, 65, "Classic", 0);
			TaskLog.info("end league:{}", 65);
		} catch (Exception e) {
			e.printStackTrace();
			TaskLog.error("league:{}, error:{}", 65, e.getMessage());
			this.insertLeagueEventSelectStat();
		}
	}

}
