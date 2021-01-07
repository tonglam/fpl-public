package com.tong.fpl.domain.letletme.player;

import com.tong.fpl.domain.letletme.entry.EntryPickData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/12/16
 */
@Data
@Accessors(chain = true)
public class PlayerPickData {

	private int entry;
	private int event;
	private List<EntryPickData> gkps;
	private List<EntryPickData> defs;
	private List<EntryPickData> mids;
	private List<EntryPickData> fwds;
	private List<EntryPickData> subs;
	private String formation;

}
