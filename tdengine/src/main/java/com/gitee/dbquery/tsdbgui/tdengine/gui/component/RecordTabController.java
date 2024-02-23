package com.gitee.dbquery.tsdbgui.tdengine.gui.component;

import com.gitee.dbquery.tsdbgui.tdengine.model.StableModel;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.util.ConnectionUtils;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.util.SuperTableUtils;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.util.TsDataUpdateUtils;
import com.gitee.dbquery.tsdbgui.tdengine.store.ApplicationStore;
import com.gitee.dbquery.tsdbgui.tdengine.util.ObjectUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.TableUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.TimeUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.TsdbConnectionUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/record_tab.fxml")
public class RecordTabController {
    @FXML
    private StackPane root;
    @FXML
    private VBox centPane;
    @FXML
    private HBox queryBox;
    @FXML
    private Pagination pagination;
    @ActionTrigger("search")
    @FXML
    private JFXButton searchButton;
    @ActionTrigger("reset")
    @FXML
    private JFXButton resetButton;

    @FXML
    private JFXDatePicker beginDatePicker;
    @FXML
    private JFXDatePicker endDatePicker;
    @FXML
    private TableView<Map<String, Object>> tableView;
    @FXML
    private Label pageInformation;
    @FXML
    private Label selectLocInfo;
    @FXML
    private JFXDialog updateRecordDialog;
    @FXML
    private VBox updateRecordPane;
    @FXML
    private String currentUpdateTbName;
    @FXML
    @ActionTrigger("updateRecordAction")
    private JFXButton updateRecordSaveButton;
    @FXML
    @ActionTrigger("closeUpdateRecordDialog")
    private JFXButton updateRecordCancelButton;

    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());

    @ActionMethod("updateRecordAction")
    private void updateRecordSaveButton() {
        StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
        Connection connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());

        List<List<Object>> dataList = new ArrayList<>();
        List<Object> data = new ArrayList<>();
        for(Node hbox: updateRecordPane.getChildren()) {
            for(Node node:((HBox)hbox).getChildren()) {
                if(node instanceof  JFXTextField) {
                    data.add(node.getUserData());
                }
            }
        }
        dataList.add(data);

        TsDataUpdateUtils.batchInsertFullColumn(connection, stableModel.getDb().getName(), currentUpdateTbName,
                dataList);


        updateRecordDialog.close();
    }

    @ActionMethod("closeUpdateRecordDialog")
    private void closeUpdateRecordDialog() {
        updateRecordDialog.close();
    }

    @PostConstruct
    public void init() {
// enable copy/paste
        TableUtils.installCopyPasteHandler(tableView);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem item = new MenuItem("复制");
        MenuItem editItem = new MenuItem("编辑");
        item.setOnAction(event -> {
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
        });

        editItem.setOnAction(event -> {
            updateRecordPane.getChildren().clear();
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            Connection connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());

            List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(), stableModel.getStb().getName());

            Map<String, Object> recordMap = tableView.getSelectionModel().getSelectedItem();
            for(TableFieldDTO field:fieldList) {
                Label label = new Label();
                label.setText(field.getName() + ":");
                label.setPadding(new Insets(0,24,0,0));
                JFXTextField textField = new JFXTextField();
                textField.setText((recordMap == null || recordMap.get(field.getName()) == null) ? "":recordMap.get(field.getName()).toString());
                textField.setUserData(ObjectUtils.stringTypeConvert(field.getDataType(), textField.getText()));
                HBox hBox = new HBox(label, textField);
                hBox.setPadding(new Insets(0,0,10,0));
                updateRecordPane.getChildren().addAll(hBox);
            }

            currentUpdateTbName = recordMap.get("tbname").toString();
            updateRecordDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            updateRecordDialog.show(root);
        });


        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(item, editItem);
        tableView.setContextMenu(menu);

        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(dataModelMapList, p -> true);
        tableView.setItems(filteredData);

        tableView.setRowFactory(new Callback<TableView<Map<String, Object>>, TableRow<Map<String, Object>>>() {
            @Override
            public TableRow<Map<String, Object>> call(TableView<Map<String, Object>> param) {
                TableRow<Map<String, Object>> tableRow = new TableRow<Map<String, Object>>();
                tableRow.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton().equals(MouseButton.PRIMARY)) {
                            ObservableList<TablePosition> posList = tableView.getSelectionModel().getSelectedCells();
                            for (TablePosition p : posList) {
                                int r = p.getRow();
                                int c = p.getColumn();
                                selectLocInfo.setText("行:" + (r + 1) + "   列:" + (c + 1));
                                break;
                            }
                        }
                    }
                });
                return tableRow;
            }
        });

        pagination.pageCountProperty().bind(pageCount);

        pagination.setPageFactory(param -> {
            showPage(param + 1);
            return tableView;
        });


        search();

    }

    @ActionMethod("search")
    private void search() {
        showPage(1);
    }


    @ActionMethod("reset")
    private void reset() {
        beginDatePicker.setValue(null);
        endDatePicker.setValue(null);
        showPage(1);
    }

    private void showPage(Integer page) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("page", page);
        query(queryMap);

    }


    private void query(Map<String, Object> queryMap) {


        Integer page = (Integer) queryMap.get("page");
        int start = (page - 1) * 1000;
        StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
        Connection connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());

        List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(), stableModel.getStb().getName());

        String whereSql = "";
        if (beginDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            whereSql = " where " + fieldList.get(0).getName() + " > " + TimeUtils.LocalDateToLong(beginDatePicker.getValue()) +
                    " and " + fieldList.get(0).getName() + " < " + TimeUtils.LocalDateToLong(endDatePicker.getValue());
        } else if (beginDatePicker.getValue() != null && endDatePicker.getValue() == null) {
            whereSql = " where " + fieldList.get(0).getName() + " > " + TimeUtils.LocalDateToLong(beginDatePicker.getValue());
        } else if (beginDatePicker.getValue() == null && endDatePicker.getValue() != null) {
            whereSql = " where " + fieldList.get(0).getName() + " < " + TimeUtils.LocalDateToLong(endDatePicker.getValue());
        }

        QueryRstDTO queryRstDTO = ConnectionUtils.executeQuery(connection, "select tbname, * from " + stableModel.getDb().getName() + "." +
                stableModel.getStb().getName() + whereSql + " limit " + start + ", " + 1000);

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

        pageCount.setValue(Integer.MAX_VALUE);
        pagination.setMaxPageIndicatorCount(3);
        pageInformation.setText("每页1000条，当前第" + (pagination.currentPageIndexProperty().get() + 1) + "页，当前页记录数（" + dataModelMapList.size() + "）");
//            QueryRstDTO countRstDTO = ConnectionUtils.executeQuery(connection, "select count(*) from " + stableModel.getDb().getName() + "." +
//                    stableModel.getStb().getName() + whereSql);
//            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
//            pageCount.setValue((total / 1000) + 1);


    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
