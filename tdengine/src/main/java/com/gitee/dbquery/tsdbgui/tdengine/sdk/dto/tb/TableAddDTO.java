package com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.tb;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.field.TableFieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @author 风一样的码农
 * @since 2023/8/18
 **/
@Data
public class TableAddDTO {
    private String db;
    private String tb;
    private List<TableFieldDTO> fieldList;
}
