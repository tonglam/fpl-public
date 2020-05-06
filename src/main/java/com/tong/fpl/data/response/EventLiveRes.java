package com.tong.fpl.data.response;

import com.tong.fpl.data.eventLive.Element;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/12
 */
@Data
public class EventLiveRes {
    private List<Element> elements;
}
