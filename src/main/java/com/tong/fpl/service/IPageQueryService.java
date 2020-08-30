package com.tong.fpl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;

/**
 * Create by tong on 2020/8/28
 */
public interface IPageQueryService {

	Page<PlayerInfoData> qryPagePlayerDataList(long current, long size);

	Page<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

}
