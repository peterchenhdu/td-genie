package com.gitee.dbquery.tsdbgui.tdengine.store;

import com.gitee.dbquery.tsdbgui.tdengine.gui.MainController;
import com.gitee.dbquery.tsdbgui.tdengine.model.ConnectionModel;
import com.zhenergy.zntsdb.common.dto.ConnectionDTO;
import com.zhenergy.zntsdb.common.util.ConnectionUtils;

import java.sql.Connection;

/**
 * @author chenpi
 * @since 1.0.0 2024/2/1 21:18
 **/
public class TsdbConnectionUtils {
    public static Connection getConnection(ConnectionModel connectionModel) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setIp(connectionModel.getIp());
        connectionDTO.setRestfulPort(connectionModel.getPort());
        connectionDTO.setUsername(connectionModel.getUsername());
        connectionDTO.setPassword(connectionModel.getPassword());
        return ConnectionUtils.getConnection(connectionDTO);
    }

    public static Connection getConnectionWithDB(ConnectionModel connectionModel, String db) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setIp(connectionModel.getIp());
        connectionDTO.setRestfulPort(connectionModel.getPort());
        connectionDTO.setUsername(connectionModel.getUsername());
        connectionDTO.setPassword(connectionModel.getPassword());
        return ConnectionUtils.getConnection(connectionDTO, db);
    }
}
