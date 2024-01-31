package com.gitee.dbquery.tsdbgui.tdengine.model;

import com.zhenergy.zntsdb.common.dto.res.DatabaseResDTO;
import com.zhenergy.zntsdb.common.dto.res.StableResDTO;
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
public class TableModel {
    private StableResDTO stb;
    private DatabaseResDTO db;
}
