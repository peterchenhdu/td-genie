package com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author chenpi
 * @since 2023/8/11
 **/
@Data
public class DatabaseCreateResDTO {
    @TdField("Database")
    private String db;
    @TdField("Create Database")
    private String createDbSql;
}
