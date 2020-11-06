package com.tong.fpl.task;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.tong.fpl.constant.enums.TournamentMode;
import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create by tong on 2020/9/16
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

	private final IReportService reportService;
	private final IQuerySerivce querySerivce;

	@Scheduled(cron = "0 0/5 * * * *")
	public void insertLeagueEventSelectStat() {
		int current = this.querySerivce.getCurrentEvent();
		if (!isNotSelectTime(current)) {
			return;
		}
		Table<Integer, String, Integer> leagueTable = this.getLeagueMap(); // league_id -> league_type -> limit
		leagueTable.rowKeySet().forEach(leagueId -> {
			String leagueType = leagueTable.row(leagueId).keySet()
					.stream()
					.findFirst()
					.orElse("");
			if (StringUtils.isEmpty(leagueType)) {
				return;
			}
			int limit = leagueTable.row(leagueId).values()
					.stream()
					.findFirst()
					.orElse(0);
			try {
				TaskLog.info("start league:{}", leagueId);
				this.reportService.insertLeagueEventSelect(current, leagueId, leagueType, limit);
				TaskLog.info("end league:{}", leagueId);
			} catch (Exception e) {
				e.printStackTrace();
				TaskLog.error("league:{}, error:{}", leagueId, e.getMessage());
				this.insertLeagueEventSelectStat();
			}
		});
	}

	private boolean isNotSelectTime(int event) {
		LocalDateTime localDateTime = LocalDateTime.parse(this.querySerivce.getDeadlineByEvent(event).replace(" ", "T"));
		return LocalDateTime.now().equals(localDateTime);
	}

	private Table<Integer, String, Integer> getLeagueMap() {
		Table<Integer, String, Integer> table = HashBasedTable.create();
		this.querySerivce.qryAllTournamentList().forEach(o -> {
			if (StringUtils.equals(TournamentMode.Normal.name(), o.getTournamentMode()) && !table.containsRow(o.getLeagueId())) {
				table.put(o.getLeagueId(), o.getLeagueType(), 0);
			}
		});
		// China
		table.put(65, "Classic", 0);
		// Overall top 10k
		table.put(314, "Classic", 10000);
		return table;
	}

}
