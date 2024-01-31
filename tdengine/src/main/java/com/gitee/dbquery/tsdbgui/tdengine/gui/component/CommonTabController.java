package com.gitee.dbquery.tsdbgui.tdengine.gui.component;

import com.gitee.dbquery.tsdbgui.tdengine.gui.MainController;
import com.gitee.dbquery.tsdbgui.tdengine.model.TableModel;
import com.zhenergy.fire.util.ObjectUtils;
import com.zhenergy.zntsdb.common.dto.QueryRstDTO;
import com.zhenergy.zntsdb.common.dto.res.DatabaseResDTO;
import com.zhenergy.zntsdb.common.dto.res.StableResDTO;
import com.zhenergy.zntsdb.common.util.ConnectionUtils;
import com.zhenergy.zntsdb.common.util.DataBaseUtils;
import com.zhenergy.zntsdb.common.util.SuperTableUtils;
import io.datafx.controller.ViewController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/common_tab.fxml")
public class CommonTabController {

    @FXML
    private Pagination pagination;

    @FXML
    private TableView<Map<String, Object>> tableView;


    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());

    @PostConstruct
    public void init() {


        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(dataModelMapList, p -> true);
        tableView.setItems(filteredData);

        pagination.pageCountProperty().bind(pageCount);
        pagination.setPageFactory(param -> {
            showPage(param + 1);
            return tableView;
        });


        if (MainController.currentNode.getType() == 0) {
            TableColumn<Map<String, Object>, String> dbNameColumn = new TableColumn<>();
            dbNameColumn.setId("nameColumn");
            dbNameColumn.setText("数据库名");
            dbNameColumn.setCellValueFactory(new MapValueFactory("name"));

            TableColumn<Map<String, Object>, String> daysColumn = new TableColumn<>();
            daysColumn.setId("daysColumn");
            daysColumn.setText("days");
            daysColumn.setCellValueFactory(new MapValueFactory("days"));

            TableColumn<Map<String, Object>, String> keepColumn = new TableColumn<>();
            keepColumn.setId("keepColumn");
            keepColumn.setText("keep");
            keepColumn.setCellValueFactory(new MapValueFactory("keep"));

            TableColumn<Map<String, Object>, String> cacheColumn = new TableColumn<>();
            cacheColumn.setId("cacheColumn");
            cacheColumn.setText("cache(MB)");
            cacheColumn.setCellValueFactory(new MapValueFactory("cache"));


            tableView.getColumns().add(dbNameColumn);
            tableView.getColumns().add(daysColumn);
            tableView.getColumns().add(keepColumn);
            tableView.getColumns().add(cacheColumn);

            List<DatabaseResDTO> dbList = DataBaseUtils.getAllDatabase(MainController.currentConnection);

            dbList.forEach(db -> {
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("name", db.getName());
                testMap.put("days", db.getDays());
                testMap.put("keep", db.getKeep());
                testMap.put("cache", db.getCache());
                dataModelMapList.add(testMap);
            });


        } else if (MainController.currentNode.getType() == 1) {
            TableColumn<Map<String, Object>, String> dbNameColumn = new TableColumn<>();
            dbNameColumn.setId("nameColumn");
            dbNameColumn.setText("超级表");
            dbNameColumn.setCellValueFactory(new MapValueFactory("name"));

            TableColumn<Map<String, Object>, String> createdTimeColumn = new TableColumn<>();
            createdTimeColumn.setId("createdTimeColumn");
            createdTimeColumn.setText("createdTime");
            createdTimeColumn.setCellValueFactory(new MapValueFactory("createdTime"));

            TableColumn<Map<String, Object>, String> columnsColumn = new TableColumn<>();
            columnsColumn.setId("columnsColumn");
            columnsColumn.setText("columns");
            columnsColumn.setCellValueFactory(new MapValueFactory("columns"));

            TableColumn<Map<String, Object>, String> tagsColumn = new TableColumn<>();
            tagsColumn.setId("tagsColumn");
            tagsColumn.setText("tags");
            tagsColumn.setCellValueFactory(new MapValueFactory("tags"));

            TableColumn<Map<String, Object>, String> tablesColumn = new TableColumn<>();
            tablesColumn.setId("tablesColumn");
            tablesColumn.setText("tables");
            tablesColumn.setCellValueFactory(new MapValueFactory("tables"));

            tableView.getColumns().add(dbNameColumn);
            tableView.getColumns().add(createdTimeColumn);
            tableView.getColumns().add(columnsColumn);
            tableView.getColumns().add(tagsColumn);
            tableView.getColumns().add(tablesColumn);

            List<StableResDTO> stbList = SuperTableUtils.getAllStable(MainController.currentConnection, MainController.currentNode.getName());

            stbList.forEach(db -> {
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("name", db.getName());
                testMap.put("createdTime", db.getCreatedTime());
                testMap.put("columns", db.getColumns());
                testMap.put("tags", db.getTags());
                testMap.put("tables", db.getTables());
                dataModelMapList.add(testMap);
            });
        } else if (MainController.currentNode.getType() == 2) {
            TableModel tableModel = (TableModel) MainController.currentNode.getData();
            QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(MainController.currentConnection, "select * from " + tableModel.getDb().getName() + "." +
                    tableModel.getStb().getName() + " limit 1, 10");

            queryRstDTO.getColumnList().forEach(column -> {
                TableColumn<Map<String, Object>, String> tmpColumn = new TableColumn<>();
                tmpColumn.setId(column + "Column");
                tmpColumn.setText(column);
                tmpColumn.setCellValueFactory(new MapValueFactory(column));
                tableView.getColumns().add(tmpColumn);
            });

            queryRstDTO.getDataList().forEach(db -> {
                db.forEach((k, v) -> {
                    if (v instanceof Byte[] || v instanceof byte[]) {
                        db.put(k, new java.lang.String((byte[]) v));
                    }
                });
                dataModelMapList.add(db);
            });

        }


    }


    private void showPage(Integer page) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("page", page);
        query(queryMap);

    }


    private void query(Map<String, Object> queryMap) {

        if (MainController.currentNode.getType() == 2) {
            Integer page = (Integer) queryMap.get("page");
            int start = (page - 1) * 20;
            TableModel tableModel = (TableModel) MainController.currentNode.getData();
            QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(MainController.currentConnection, "select * from " + tableModel.getDb().getName() + "." +
                    tableModel.getStb().getName() + " limit " + start + ", " + 20);

            tableView.getColumns().clear();
            queryRstDTO.getColumnList().forEach(column -> {
                TableColumn<Map<String, Object>, String> tmpColumn = new TableColumn<>();
                tmpColumn.setId(column + "Column");
                tmpColumn.setText(column);
                tmpColumn.setCellValueFactory(new MapValueFactory(column));
                tableView.getColumns().add(tmpColumn);
            });

            dataModelMapList.clear();
            queryRstDTO.getDataList().forEach(db -> {
                db.forEach((k, v) -> {
                    if (v instanceof Byte[] || v instanceof byte[]) {
                        db.put(k, new java.lang.String((byte[]) v));
                    }
                });
                dataModelMapList.add(db);
            });


            QueryRstDTO countRstDTO = ConnectionUtils.executeQuery(MainController.currentConnection, "select count(*) from " + tableModel.getDb().getName() + "." +
                    tableModel.getStb().getName());
            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
            pageCount.setValue((total / 20) + 1);
        }

    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
