package com.gitee.dbquery.tsdbgui.tdengine.model;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.StableResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TableModel
 *
 * @author pc
 * @since 2024/01/31
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StableModel {
    private StableResDTO stb;
    private DatabaseModel db;

    @Override
    public String toString() {
        return stb.getName() + "@" + db.getName() + "@" + db.getConnectionModel().getName();
    }
}
