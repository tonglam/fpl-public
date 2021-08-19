package com.tong.fpl.config.mp;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.tong.fpl.constant.enums.DynamicTableName;
import com.tong.fpl.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Create by tong on 2020/4/26
 */
@Configuration
@MapperScan("com.tong.fpl.mapper")
public class MybatisPlusConfig {

    public static ThreadLocal<String> season = new ThreadLocal<>();

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandlerMap(new HashMap<String, TableNameHandler>(2) {
            private static final long serialVersionUID = -7337175818692170155L;

            {
                Arrays.stream(DynamicTableName.values()).forEach(dynamicTableName ->
                        this.put(dynamicTableName.getTableName(), (sql, tableName) -> {
                            String season = MybatisPlusConfig.season.get();
                            if (StringUtils.isBlank(season) || StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
                                return tableName;
                            }
                            return tableName + "_" + season;
                        }));
            }
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

    @Bean
    public MybatisPlusInjector sqlInjector() {
        return new MybatisPlusInjector();
    }

}
