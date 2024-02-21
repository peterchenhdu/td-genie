package com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.tb;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.field.TableFieldDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author chenpi
 * @since 2023/8/23
 **/
@Data
public class TableUpdateDTO {
    private String db;
    private String tb;
    private List<TableFieldDTO> addList;
    private List<TableFieldDTO> deleteList;
    private List<TableFieldDTO> updateList;
    /**
     *  Tag Name修改Map
     */
    private Map<String, String> tagNameChangeMap;
}
