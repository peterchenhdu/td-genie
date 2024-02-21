package com.gitee.dbquery.tsdbgui.tdengine.store;

import com.gitee.dbquery.tsdbgui.tdengine.model.CommonNode;
import com.gitee.dbquery.tsdbgui.tdengine.model.ConnectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ApplicationStore
 *
 * @author pc
 * @since 2024/01/31
 **/
@Data
public class ApplicationStore {
    private static TreeItem<CommonNode> connectionTree;
    private static CommonNode currentNode;
    private static TreeItem<CommonNode> currentTreeItem;
    private static HashMap<String, Tab> tabsMap = new HashMap<>();


    public static void connectionTbCheck() throws SQLException {
        List<String> tableNameList = new ArrayList<>();
        List<Map<String, Object>> tables = H2DbUtils.query("show tables;");
        for (Map<String, Object> tb : tables) {
            tableNameList.add(tb.get("TABLE_NAME").toString());
        }

        if (!tableNameList.contains("t_connection".toUpperCase())) {
            Map<String, Object> fieldMap = new HashMap<>();
            fieldMap.put("name", String.class);
            fieldMap.put("ip", String.class);
            fieldMap.put("port", String.class);
            fieldMap.put("username", String.class);
            fieldMap.put("password", String.class);
            H2DbUtils.createTable("t_connection", fieldMap);
        }
    }

    public static List<ConnectionModel> getConnectionList() throws SQLException {
        List<Map<String, Object>> connectionList = H2DbUtils.query("select * from  t_connection;");
        return connectionList.stream().map(con -> {
            ConnectionModel connectionDTO = new ConnectionModel();
            connectionDTO.setIp(con.get("IP").toString());
            connectionDTO.setPort(con.get("PORT").toString());
            connectionDTO.setUsername(con.get("USERNAME").toString());
            connectionDTO.setPassword(con.get("PASSWORD").toString());
            connectionDTO.setName(con.get("NAME").toString());
            return connectionDTO;
        }).collect(Collectors.toList());
    }


    public static TreeItem<CommonNode> getConnectionTree() {
        return connectionTree;
    }

    public static void setConnectionTree(TreeItem<CommonNode> connectionTree) {
        ApplicationStore.connectionTree = connectionTree;
    }

    public static CommonNode getCurrentNode() {
        return currentTreeItem == null ? null : currentTreeItem.getValue();
    }

    public static HashMap<String, Tab> getTabsMap() {
        return tabsMap;
    }

    public static void setTabsMap(HashMap<String, Tab> tabsMap) {
        ApplicationStore.tabsMap = tabsMap;
    }

    public static TreeItem<CommonNode> getCurrentTreeItem() {
        return currentTreeItem;
    }

    public static void setCurrentTreeItem(TreeItem<CommonNode> currentTreeItem) {
        ApplicationStore.currentTreeItem = currentTreeItem;
    }
}


