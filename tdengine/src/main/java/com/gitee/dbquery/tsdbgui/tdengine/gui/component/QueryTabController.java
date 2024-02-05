package com.gitee.dbquery.tsdbgui.tdengine.gui.component;

import com.gitee.dbquery.tsdbgui.tdengine.gui.MainController;
import com.gitee.dbquery.tsdbgui.tdengine.model.CommonNode;
import com.gitee.dbquery.tsdbgui.tdengine.model.ConnectionModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.DatabaseModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.TableModel;
import com.gitee.dbquery.tsdbgui.tdengine.store.TsdbConnectionUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.TableUtils;
import com.jfoenix.controls.JFXComboBox;
import com.zhenergy.fire.util.ObjectUtils;
import com.zhenergy.zntsdb.common.dto.QueryRstDTO;
import com.zhenergy.zntsdb.common.util.ConnectionUtils;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/query_tab.fxml")
public class QueryTabController {

    @FXML
    private Pagination pagination;

    @FXML
    private TableView<Map<String, Object>> tableView;
    @FXML
    private SplitPane querySplitPane;
    @FXML
    private JFXComboBox<String> connectionComboBox;
    @FXML
    private JFXComboBox<String> dbComboBox;
    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());

    @PostConstruct
    public void init() {
        if(MainController.connectionTree != null) {
            MainController.connectionTree.getChildren().forEach(d->{
                connectionComboBox.getItems().add(d.getValue().getName());
            });
        }

        connectionComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println(newValue);

                    for(TreeItem<CommonNode> node :  MainController.connectionTree.getChildren()) {
                        if(node.getValue().getName().equals(newValue)) {
                            node.getChildren().forEach(d->{
                                dbComboBox.getItems().add(d.getValue().getName());
                            });
                        }
                    }

                });

        if (MainController.currentNode.getType() == 0) {
            ConnectionModel connectionModel = (ConnectionModel) MainController.currentNode.getData();

            connectionComboBox.getSelectionModel().select(connectionModel.getName());
        } else if (MainController.currentNode.getType() == 1) {
            DatabaseModel databaseModel = (DatabaseModel) MainController.currentNode.getData();

            connectionComboBox.getSelectionModel().select(databaseModel.getConnectionModel().getName());
            dbComboBox.getSelectionModel().select(databaseModel.getName());
        } else if (MainController.currentNode.getType() == 2) {
            TableModel tableModel = (TableModel) MainController.currentNode.getData();
        }





        querySplitPane.setDividerPositions(0.7);

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
                    if (cell == null)
                        cell = "";
                    if (old_r == r)
                        clipboardString.append('\t');
                    else if (old_r != -1)
                        clipboardString.append('\n');
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


//        TableModel tableModel = (TableModel) MainController.currentNode.getData();
//        Connection connection = TsdbConnectionUtils.getConnection(tableModel.getDb().getConnectionModel());
//        QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(connection, "select * from " + tableModel.getDb().getName() + "." +
//                tableModel.getStb().getName() + " limit 1, 10");
//
//        queryRstDTO.getColumnList().forEach(column -> {
//            TableColumn<Map<String, Object>, String> tmpColumn = new TableColumn<>();
//            tmpColumn.setId(column + "Column");
//            tmpColumn.setText(column);
//            tmpColumn.setCellValueFactory(new MapValueFactory(column));
//            tableView.getColumns().add(tmpColumn);
//        });
//
//        queryRstDTO.getDataList().forEach(db -> {
//            db.forEach((k, v) -> {
//                if (v instanceof Byte[] || v instanceof byte[]) {
//                    db.put(k, new String((byte[]) v));
//                }
//            });
//            dataModelMapList.add(db);
//        });


    }


    private void showPage(Integer page) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("page", page);
        query(queryMap);

    }


    private void query(Map<String, Object> queryMap) {

        if (MainController.currentNode.getType() == 2) {
            Integer page = (Integer) queryMap.get("page");
            int start = (page - 1) * 1000;
            TableModel tableModel = (TableModel) MainController.currentNode.getData();
            Connection connection = TsdbConnectionUtils.getConnection(tableModel.getDb().getConnectionModel());
            QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(connection, "select * from " + tableModel.getDb().getName() + "." +
                    tableModel.getStb().getName() + " limit " + start + ", " + 1000);

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


            QueryRstDTO countRstDTO = ConnectionUtils.executeQuery(connection, "select count(*) from " + tableModel.getDb().getName() + "." +
                    tableModel.getStb().getName());
            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
            pageCount.setValue((total / 1000) + 1);
        }

    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
