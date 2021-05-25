package com.tong.fpl.domain.letletme.summary;

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
    private int mostTransfersCost; // 转会最多剁手
    private List<EntryEventTransfersData> mostTransfers; // 转会最多
    private List<EntryEventTransfersData> mostTransfersIn; // 最多转入
    private List<EntryEventTransfersData> mostTransfersOut; // 最多转出
    private List<EntryEventTransfersData> mostTransferInPointsList; // 最大转入得分
    private List<EntryEventTransfersData> negativeTransferInPointsList; // 转入负分
    private List<EntryEventTransfersData> mostTransferOutPointsList; // 最大转出得分
    private List<EntryEventTransfersData> bestTransferInList; // 最大收益转入
    private List<EntryEventTransfersData> worstTransferInList; // 最小收益转入

}
