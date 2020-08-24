package com.tong.fpl.service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/8/23
 */
public interface ICacheSerive {

    void insertTeam();

    void insertHisTeam(String season);

    void insertEvent();

    void insertHisEvent(String season);

    void insertPlayer();

    void insertHisPlayer(String season);

    void insertEventFixture();

    void insertPlayerValue();

    <T> String createKey(T entity, String keyName, Object key);

    <T> String createKey(String season, T entity, String keyName, Object key);

    void setCache(Map<String, Object> cacheMap, long expireSeconds);

    List<Object> getMultiValues(String pattern);

}
