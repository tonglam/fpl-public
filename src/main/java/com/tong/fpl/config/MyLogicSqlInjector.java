package com.tong.fpl.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.tong.fpl.db.methods.TruncateTable;

import java.util.List;

/**
 * Create by tong on 2020/4/26
 */
public class MyLogicSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new TruncateTable());
        return methodList;
    }
}
