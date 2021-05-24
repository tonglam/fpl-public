package com.tong.fpl.domain.letletme.summary;

import com.tong.fpl.domain.letletme.entry.EntryEventTransfersData;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntryTransfersData {

    private int mostTransfers;
    private int mostTransfersEvent;
    private int mostTransfersCost;
    private EntryEventTransfersData bestTransfer;
    private EntryEventTransfersData worstTransfer;

}
