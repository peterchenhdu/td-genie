package com.gitee.dbquery.tsdbgui.tdengine.sdk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author chenpi
 * @since 2022/3/7
 **/
@AllArgsConstructor
@Data
public class QueryRstDTO {
    private List<String> columnList;
    private List<Map<String, Object>> dataList;
}
