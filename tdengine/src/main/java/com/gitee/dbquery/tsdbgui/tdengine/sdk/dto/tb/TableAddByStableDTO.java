package com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.tb;

import lombok.Data;

import java.util.Map;

/**
 * @author chenpi
 * @since 2023/8/23
 **/
@Data
public class TableAddByStableDTO {
    private String dbName;
    private String tableName;
    private String stableName;
    private Map<String, String> tagValueMap;

}
