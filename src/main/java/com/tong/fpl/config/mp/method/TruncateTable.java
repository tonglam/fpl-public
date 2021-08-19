package com.tong.fpl.config.mp.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * Create by tong on 2021/8/19
 */
public class TruncateTable extends AbstractMethod {

    private static final long serialVersionUID = -1383127964205871515L;

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = "TRUNCATE TABLE " + tableInfo.getTableName();
        /* mapper 接口方法名一致 */
        String method = "truncate";
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
        return this.addDeleteMappedStatement(mapperClass, method, sqlSource);
    }

}
