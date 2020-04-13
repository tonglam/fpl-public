package com.tong.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Create by tong on 2020/1/19
 */
public class BaseService {

    @Autowired
    protected MongoTemplate mongoTemplate;
}
