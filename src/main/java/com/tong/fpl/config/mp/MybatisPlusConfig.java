package com.tong.fpl.config.mp;

import com.baomidou.mybatisplus.extension.parsers.DynamicTableNameParser;
import com.baomidou.mybatisplus.extension.parsers.ITableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.tong.fpl.constant.enums.DynamicTableName;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Create by tong on 2020/4/26
 */
@Configuration
@MapperScan("com.tong.fpl.mapper")
public class MybatisPlusConfig {

	public static ThreadLocal<String> tableSeason = new ThreadLocal<>();

	@Bean
	public PaginationInterceptor paginationInterceptor() {
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		DynamicTableNameParser dynamicTableNameParser = new DynamicTableNameParser();
		dynamicTableNameParser.setTableNameHandlerMap(new HashMap<String, ITableNameHandler>(2) {
			private static final long serialVersionUID = -7337175818692170155L;

			{
				Arrays.stream(DynamicTableName.values()).forEach(dynamicTableName ->
						put(dynamicTableName.getTableName(), (metaObject, sql, tableName) -> {
							String season = tableSeason.get();
							if (StringUtils.isBlank(season)) {
								return tableName;
							}
							return tableName + "_" + season;
						}));
			}
		});
		paginationInterceptor.setSqlParserList(Collections.singletonList(dynamicTableNameParser));
		return paginationInterceptor;
	}

}
