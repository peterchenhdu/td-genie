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
    private String port;
    private String username;
    private String password;
    private List<DatabaseModel> dbList;

    @Override
    public String toString() {
        return name;
    }
}
