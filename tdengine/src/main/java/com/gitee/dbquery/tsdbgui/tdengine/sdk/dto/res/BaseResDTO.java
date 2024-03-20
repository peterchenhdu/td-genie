package com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author chenpi
 * @since 2024/3/20
 **/
@Data
public class BaseResDTO {
    private String status;
    private Integer code;
    private String desc;
    private Integer rows;
    private List<String> head;
    @JSONField(name = "column_meta")
    private List<List<Object>> columnMeta;
    private List<List<Object>> data;
}
