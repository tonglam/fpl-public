package com.tong.fpl.domain.letletme.tournament;

import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/11/4
 */
@Data
@Accessors(chain = true)
public class TournamentEventCaptainData {

	private int tournamentId;
	private int event;
	private List<EntryEventCaptainData> entryCaptainDataList;

}
