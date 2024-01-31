package com.gitee.dbquery.tsdbgui.tdengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ConnectionModel
 *
 * @author pc
 * @since 2024/01/31
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConnectionModel {
    private String name;
    private String ip;
    private Integer port;
    private List<DatabaseModel> dbList;
}
