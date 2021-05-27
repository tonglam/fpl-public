package com.tong.fpl.domain.letletme.summary.league;

import com.tong.fpl.domain.letletme.entry.EntryEventTransfersData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Create by tong on 2021/5/27
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonTransfersData {

    private int leagueId;
    private String leagueType;
    private String leagueName;
    private int averageTransfers; // 平均转会数
    private int averageTransfersCost; // 平均剁手
    private int mostTransfersEvent; // 转会最多周
    private int mostTransfersCostEvent; // 转会最多剁手周
    private List<EntryEventTransfersData> bestTransfers; // 最佳转会
    private List<EntryEventTransfersData> worstTransfers; // 最差转会
    private LinkedHashMap<String, Long> mostTransfersIn; // 最多转入球员
    private LinkedHashMap<String, Long> mostTransfersOut; // 最多转出
    private LinkedHashMap<EntryEventTransfersData, Long> negativeTransferInPoints; // 转入负分

}
