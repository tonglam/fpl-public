package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.ElementLiveData;

/**
 * Create by tong on 2020/8/3
 */
public interface ILiveApi {

	TableData<ElementLiveData> qryEntryLivePoints(int entry);

}
