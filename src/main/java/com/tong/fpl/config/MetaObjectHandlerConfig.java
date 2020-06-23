package com.tong.fpl.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Create by tong on 2020/4/27
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
	    this.setFieldValByName("create_time", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
	    this.setFieldValByName("update_time", new Date(), metaObject);
    }

}
