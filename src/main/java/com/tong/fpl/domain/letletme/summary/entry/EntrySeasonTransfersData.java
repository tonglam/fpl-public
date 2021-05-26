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
    private List<EntryTransfersCostData> transfersCost; // 剁手周
    private int mostTransfersEvent; // 转会最多剁手周
    private int mostTransfersCost; // 转会最多剁手
    private int mostTransfersProfit; // 转会最多收益
    private List<EntryEventTransfersData> mostTransfers; // 转会最多
    private String mostTransfersInWebName; // 最多转入球员
    private List<EntryEventTransfersData> mostTransfersIn; // 最多转入
    private String mostTransfersOutWebName; // 最多转出球员
    private List<EntryEventTransfersData> mostTransfersOut; // 最多转出
    private int mostTransferInPointsEvent; // 最大转入得分周
    private String mostTransferInPointsWebName; // 最大转入得分球员
    private List<EntryEventTransfersData> mostTransferInPoints; // 最大转入得分
    private int mostTransferOutPointsEvent; // 最大转出得分周
    private String mostTransferOutPointsWebName; // 最大转出得分球员
    private List<EntryEventTransfersData> mostTransferOutPoints; // 最大转出得分
    private List<EntryEventTransfersData> negativeTransferInPoints; // 转入负分
    private int bestTransfersEvent; // 最大收益手周
    private int bestTransfersProfit; // 最大收益
    private List<EntryEventTransfersData> bestTransfers; // 最大收益转入
    private int worstTransfersEvent; // 最小收益周
    private int worstTransfersProfit; // 最小收益
    private List<EntryEventTransfersData> worstTransfers; // 最小收益转入


}
