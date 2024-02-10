package com.gitee.dbquery.tsdbgui.tdengine.gui.component;

import com.gitee.dbquery.tsdbgui.tdengine.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tsdbgui.tdengine.model.ConnectionModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.DatabaseModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.StableModel;
import com.gitee.dbquery.tsdbgui.tdengine.store.ApplicationStore;
import com.gitee.dbquery.tsdbgui.tdengine.util.TsdbConnectionUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.TableUtils;
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
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/stb_tab.fxml")
public class StbTabController {
    @FXML
    private VBox centPane;
    @FXML
    private HBox queryBox;
    @FXML
    private Pagination pagination;

    @FXML
    private TableView<Map<String, Object>> tableView;


    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());

    @PostConstruct
    public void init() {
// enable copy/paste
        TableUtils.installCopyPasteHandler(tableView);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem item = new MenuItem("复制");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TablePosition> posList = tableView.getSelectionModel().getSelectedCells();
                int old_r = -1;
                StringBuilder clipboardString = new StringBuilder();
                for (TablePosition p : posList) {
                    int r = p.getRow();
                    int c = p.getColumn();
                    Object cell = tableView.getColumns().get(c).getCellData(r);
                    if (cell == null) {
                        cell = "";
                    }
                    if (old_r == r) {
                        clipboardString.append('\t');
                    } else if (old_r != -1) {
                        clipboardString.append('\n');
                    }
                    clipboardString.append(cell);
                    old_r = r;
                }
                final ClipboardContent content = new ClipboardContent();
                content.putString(clipboardString.toString());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(item);
        tableView.setContextMenu(menu);

        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(dataModelMapList, p -> true);
        tableView.setItems(filteredData);

        pagination.pageCountProperty().bind(pageCount);
        pagination.setPageFactory(param -> {
            showPage(param + 1);
            return tableView;
        });


        if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.CONNECTION) {
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

            TableColumn<Map<String, Object>, String> blocksColumn = new TableColumn<>();
            blocksColumn.setId("blocksColumn");
            blocksColumn.setText("blocks");
            blocksColumn.setCellValueFactory(new MapValueFactory("blocks"));

            TableColumn<Map<String, Object>, String> quorumColumn = new TableColumn<>();
            quorumColumn.setId("quorumColumn");
            quorumColumn.setText("quorum");
            quorumColumn.setCellValueFactory(new MapValueFactory("quorum"));

            TableColumn<Map<String, Object>, String> compColumn = new TableColumn<>();
            compColumn.setId("compColumn");
            compColumn.setText("comp");
            compColumn.setCellValueFactory(new MapValueFactory("comp"));

            TableColumn<Map<String, Object>, String> walLevelColumn = new TableColumn<>();
            walLevelColumn.setId("walLevelColumn");
            walLevelColumn.setText("walLevelColumn");
            walLevelColumn.setCellValueFactory(new MapValueFactory("walLevel"));

            TableColumn<Map<String, Object>, String> fsyncColumn = new TableColumn<>();
            fsyncColumn.setId("fsyncColumn");
            fsyncColumn.setText("fsync");
            fsyncColumn.setCellValueFactory(new MapValueFactory("fsync"));

            TableColumn<Map<String, Object>, String> replicaColumn = new TableColumn<>();
            replicaColumn.setId("replicaColumn");
            replicaColumn.setText("replica");
            replicaColumn.setCellValueFactory(new MapValueFactory("replica"));

            TableColumn<Map<String, Object>, String> updateColumn = new TableColumn<>();
            updateColumn.setId("updateColumn");
            updateColumn.setText("update");
            updateColumn.setCellValueFactory(new MapValueFactory("update"));

            TableColumn<Map<String, Object>, String> cacheLastColumn = new TableColumn<>();
            cacheLastColumn.setId("cacheLastColumn");
            cacheLastColumn.setText("cacheLast");
            cacheLastColumn.setCellValueFactory(new MapValueFactory("cacheLast"));

            TableColumn<Map<String, Object>, String> minRowsColumn = new TableColumn<>();
            minRowsColumn.setId("minRowsColumn");
            minRowsColumn.setText("minRows");
            minRowsColumn.setCellValueFactory(new MapValueFactory("minRows"));

            TableColumn<Map<String, Object>, String> maxRowsColumn = new TableColumn<>();
            maxRowsColumn.setId("maxRowsColumn");
            maxRowsColumn.setText("maxRows");
            maxRowsColumn.setCellValueFactory(new MapValueFactory("maxRows"));

            TableColumn<Map<String, Object>, String> precisionColumn = new TableColumn<>();
            precisionColumn.setId("precisionColumn");
            precisionColumn.setText("precision");
            precisionColumn.setCellValueFactory(new MapValueFactory("precision"));

            tableView.getColumns().add(dbNameColumn);
            tableView.getColumns().add(daysColumn);
            tableView.getColumns().add(keepColumn);
            tableView.getColumns().add(cacheColumn);
            tableView.getColumns().add(blocksColumn);
            tableView.getColumns().add(quorumColumn);
            tableView.getColumns().add(compColumn);
            tableView.getColumns().add(walLevelColumn);
            tableView.getColumns().add(fsyncColumn);
            tableView.getColumns().add(replicaColumn);
            tableView.getColumns().add(cacheLastColumn);
            tableView.getColumns().add(minRowsColumn);
            tableView.getColumns().add(maxRowsColumn);
            tableView.getColumns().add(precisionColumn);

            ConnectionModel connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();




            List<DatabaseResDTO> dbList = DataBaseUtils.getAllDatabase(TsdbConnectionUtils.getConnection(connectionModel));

            dbList.forEach(db -> {
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("name", db.getName());
                testMap.put("days", db.getDays());
                testMap.put("keep", db.getKeep());
                testMap.put("cache", db.getCache());
                testMap.put("blocks", db.getBlocks());
                testMap.put("quorum", db.getQuorum());
                testMap.put("comp", db.getComp());
                testMap.put("walLevel", db.getWalLevel());
                testMap.put("fsync", db.getFsync());
                testMap.put("replica", db.getReplica());
                testMap.put("update", db.getUpdate());
                testMap.put("cacheLast", db.getCacheLast());
                testMap.put("minRows", db.getMinRows());
                testMap.put("maxRows", db.getMaxRows());
                testMap.put("precision", db.getPrecision());
                dataModelMapList.add(testMap);
            });

            centPane.getChildren().removeAll(queryBox);
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.DB) {
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


            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            Connection connection = TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel());

            List<StableResDTO> stbList = SuperTableUtils.getAllStable(connection, ApplicationStore.getCurrentNode().getName());

            stbList.forEach(db -> {
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("name", db.getName());
                testMap.put("createdTime", db.getCreatedTime());
                testMap.put("columns", db.getColumns());
                testMap.put("tags", db.getTags());
                testMap.put("tables", db.getTables());
                dataModelMapList.add(testMap);
            });

            centPane.getChildren().removeAll(queryBox);
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.STB) {
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            Connection connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());
            QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(connection, "select * from " + stableModel.getDb().getName() + "." +
                    stableModel.getStb().getName() + " limit 1, 10");

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
                        db.put(k, new String((byte[]) v));
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

        if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.STB) {
            Integer page = (Integer) queryMap.get("page");
            int start = (page - 1) * 1000;
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            Connection connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());
            QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(connection, "select * from " + stableModel.getDb().getName() + "." +
                    stableModel.getStb().getName() + " limit " + start + ", " + 1000);

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
                        db.put(k, new String((byte[]) v));
                    }
                });
                dataModelMapList.add(db);
            });


            QueryRstDTO countRstDTO = ConnectionUtils.executeQuery(connection, "select count(*) from " + stableModel.getDb().getName() + "." +
                    stableModel.getStb().getName());
            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
            pageCount.setValue((total / 1000) + 1);
        }

    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
