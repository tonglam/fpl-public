package com.tong.fpl.config.mp;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.tong.fpl.config.mp.method.TruncateTable;

import java.util.List;

/**
 * Create by tong on 2021/8/19
 */
public class MybatisPlusInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        //增加自定义方法
        methodList.add(new TruncateTable());
        return methodList;
    }
}
