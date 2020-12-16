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
public class PickPlayerData {

	private List<EntryPickData> gkp;
	private List<EntryPickData> def;
	private List<EntryPickData> mid;
	private List<EntryPickData> fwd;
	private List<EntryPickData> sub;

}
