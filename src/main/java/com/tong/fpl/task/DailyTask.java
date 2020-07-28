package com.tong.fpl.task;

import com.tong.fpl.service.IUpdateEventResultsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/21
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailyTask {

	private final IUpdateEventResultsService updateEventResultsService;

	@Scheduled(cron = "0 14 19 * * ?")
	public void updateEventResult() {
		IntStream.range(12, 15).forEach(event -> {
			log.info("start event: " + event);
			this.updateEventResultsService.updateTournamentEntryEventResult(event, 12);
		});
	}

}
