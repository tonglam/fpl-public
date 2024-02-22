package com.tong.fpl.domain.letletme.summary.entry;

import com.tong.fpl.domain.letletme.entry.EntryEventTransfersData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntrySeasonTransfersData {

    private int entry;
    private String entryName;
    private String playerName;
    private List<EntryEventTransfersData> bestTransfers; // 最佳转会
    private List<EntryEventTransfersData> worstTransfers; // 最差转会
    private String mostTransfersInWebName; // 最多转入球员
    private List<EntryEventTransfersData> mostTransfersIn; // 最多转入
    private String mostTransfersOutWebName; // 最多转出球员
    private List<EntryEventTransfersData> mostTransfersOut; // 最多转出
    private List<EntryEventTransfersData> negativeTransferInPoints; // 转入负分
    private int mostTransfersEvent; // 转会最多周
    private int mostTransfersCost; // 转会最多剁手
    private int mostTransfersProfit; // 转会最多收益
    private List<EntryEventTransfersData> mostTransfers; // 转会最多
    private List<EntryTransfersCostData> transfersCost; // 剁手周

}
