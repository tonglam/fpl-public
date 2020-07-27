package com.tong.fpl.api;

import com.tong.fpl.domain.web.PlayerValueData;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
public interface IHttpApi {

    List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

}
