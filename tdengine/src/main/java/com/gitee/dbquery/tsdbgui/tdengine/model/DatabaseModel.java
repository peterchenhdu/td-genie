package com.gitee.dbquery.tsdbgui.tdengine.model;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.DatabaseResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.StableResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DatabaseModel
 *
 * @author pc
 * @since 2024/01/31
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DatabaseModel {
    private String name;
    private DatabaseResDTO databaseResDTO;
    private ConnectionModel connectionModel;

    @Override
    public String toString() {
        return name + "@" + connectionModel.getName();
    }
}
