package com.tong.fpl.task;

import com.tong.fpl.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/9/16
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

	private final IReportService reportService;

	@Scheduled(cron = "0 27 19 * * *")
	public void insertLeagueResultStat() {
		log.info("start insertLeagueResultStat task");
		try {
			this.reportService.insertLeagueResultStat(1, "Classic", 314, 10000);
		} catch (Exception e) {
			log.error("insertLeagueResultStat task error:{}", e.getMessage());
			this.insertLeagueResultStat();
		}
	}

}
