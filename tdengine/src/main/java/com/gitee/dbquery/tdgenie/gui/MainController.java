package com.gitee.dbquery.tdgenie.gui;

import cn.hutool.core.io.FileUtil;
import com.gitee.dbquery.tdgenie.AppStartup;
import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.gui.component.*;
import com.gitee.dbquery.tdgenie.model.CommonNode;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import com.gitee.dbquery.tdgenie.model.DatabaseModel;
import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.db.DbConfigAddDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.db.DbConfigUpdateDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.stb.StableAddDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.stb.StableUpdateDTO;
import com.gitee.dbquery.tdgenie.sdk.util.DataBaseUtils;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.sdk.util.SuperTableUtils;
import com.gitee.dbquery.tdgenie.sdk.util.VersionUtils;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.gitee.dbquery.tdgenie.store.H2DbUtils;
import com.gitee.dbquery.tdgenie.util.*;
import com.jfoenix.controls.*;
import io.datafx.controller.ViewController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.container.AnimatedFlowContainer;
import io.datafx.controller.flow.container.ContainerAnimations;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MainController
 *
 * @author pc
 * @since 2024/01/31
 **/
@Slf4j
@ViewController("/fxml/main.fxml")
public class MainController {
    @FXML
    private StackPane rootPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private JFXTreeView<CommonNode> leftTreeView;
    @FXML
    private JFXTabPane tabPane;
    @FXML
    private GridPane createTbPane;
    @FXML
    @ActionTrigger("createConnectionAction")
    private MenuItem createConnectionMenuItem;
    @FXML
    private VBox createConnectionBox;
    @FXML
    private VBox connectionsBox;
    @FXML
    private VBox queryMonitorBox;
    @FXML
    private VBox userQueryBox;
    @FXML
    private VBox createQueryBox;
    @FXML
    private VBox resourceMonitorBox;
    @FXML
    private VBox clusterBox;
    @FXML
    @ActionTrigger("exitAction")
    private MenuItem exitMenuItem;
    @FXML
    @ActionTrigger("createQueryAction")
    private MenuItem createQueryMenuItem;
    @FXML
    @ActionTrigger("aboutAction")
    private MenuItem aboutMenuItem;
    @FXML
    @ActionTrigger("clusterQueryAction")
    private MenuItem clusterQueryMenuItem;
    @FXML
    @ActionTrigger("userQueryAction")
    private MenuItem userQueryMenuItem;
    @FXML
    @ActionTrigger("resourceMonitorAction")
    private MenuItem resourceMonitorMenuItem;
    @FXML
    @ActionTrigger("connectionMonitorAction")
    private MenuItem connectionMonitorMenuItem;
    @FXML
    @ActionTrigger("queryMonitorAction")
    private MenuItem queryMonitorMenuItem;
    @FXML
    private JFXDialog dialog;
    @FXML
    private Label dialogTitle;
    @FXML
    private Label createDbDialogTitle;
    @FXML
    private Label createTbDialogTitle;
    @FXML
    private JFXDialog createDbDialog;
    @FXML
    private JFXDialog createTbDialog;
    @FXML
    private JFXDialog aboutDialog;
    @FXML
    @ActionTrigger("saveConnection")
    private JFXButton saveButton;
    @FXML
    @ActionTrigger("closeDialog")
    private JFXButton cancelButton;

    @FXML
    @ActionTrigger("closeCreateDbDialog")
    private JFXButton createDBCancelButton;
    @FXML
    @ActionTrigger("createDB")
    private JFXButton createDBSaveButton;
    @FXML
    @ActionTrigger("addField")
    private JFXButton addFieldButton;

    @FXML
    @ActionTrigger("closeCreateTbDialog")
    private JFXButton createTBCancelButton;
    @FXML
    @ActionTrigger("createTB")
    private JFXButton createTBSaveButton;

    @FXML
    private JFXTextField nameTextField;
    @FXML
    private JFXTextField ipTextField;
    @FXML
    private JFXTextField portTextField;
    @FXML
    private JFXTextField usernameTextField;
    @FXML
    private JFXTextField passwordTextField;


    @FXML
    private JFXTextField createDbName;
    @FXML
    private JFXTextField createDbReplica;
    @FXML
    private JFXTextField createDbDays;
    @FXML
    private JFXTextField createDbBlocks;


    @FXML
    private JFXTextField tableName0_TextField;

    private int nextRowIndex = 2;

    private TreeItem<CommonNode> root;







    @PostConstruct
    public void init() throws SQLException {
        ApplicationContext.getInstance().register(this, MainController.class);

        //connection tb exist check
        ApplicationStore.connectionTbExistCheck();
        tabPane.setContextMenu(ContextMenuUtils.generateTabPaneContextMenu(tabPane));
        //记录最后一次dividerPositions
        splitPane.getDividers().get(0).positionProperty().addListener((o, oldPos, newPos) -> AppStartup.dividerPositions = newPos.doubleValue());

        createConnectionBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            createConnectionAction();
        });

        createQueryBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            createQueryAction();
        });

        resourceMonitorBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            resourceMonitorAction();
        });

        clusterBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("clusterBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }


            clusterQueryAction();
        });

        userQueryBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("userQueryBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }

            userQueryAction();

        });
        queryMonitorBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("queryMonitorBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }


            queryMonitorAction();
        });
        connectionsBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("connectionsBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }


           connectionMonitorAction();
        });

        leftTreeView.setMinWidth(100);
        root = new TreeItem<>(new CommonNode("td-genie", NodeTypeEnum.ROOT, null), ImageViewUtils.getImageViewByType(NodeTypeEnum.ROOT));
        root.setExpanded(true);
        ApplicationStore.setConnectionTree(root);
        leftTreeView.setRoot(root);
        leftTreeView.setShowRoot(false);

        List<ConnectionModel> connectionNodeList = ApplicationStore.getConnectionList();
        for (ConnectionModel connectionModel : connectionNodeList) {
            root.getChildren().add(TreeUtils.generateConnectionTree(connectionModel));
        }


        MenuItem createConnectionMenuItem = new MenuItem("新建连接");
        createConnectionMenuItem.setOnAction((ActionEvent t) -> {
            dialogTitle.setText("新建连接");
            showAddConnectionDialog();
        });

        MenuItem updateConnectionMenuItem = new MenuItem("编辑连接");
        updateConnectionMenuItem.setOnAction((ActionEvent t) -> {
            dialogTitle.setText("编辑连接");
            showAddConnectionDialog();
        });

        MenuItem deleteConnectionMenuItem = new MenuItem("删除连接");
        deleteConnectionMenuItem.setOnAction((ActionEvent t) -> {

            try {
                H2DbUtils.executeUpdate("delete from t_connection where name='" + ApplicationStore.getCurrentNode().getName() + "';");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showException(e, rootPane);
            }
            for (TreeItem<CommonNode> treeItem : root.getChildren()) {
                if (treeItem.getValue().getName().equals(ApplicationStore.getCurrentNode().getName())) {
                    root.getChildren().remove(treeItem);
                    break;
                }
            }

        });

        MenuItem exportSQLMenuItem = new MenuItem("导出SQL");
        exportSQLMenuItem.setOnAction(this::handExportDateAction);

        MenuItem createQueryMenuItem = new MenuItem("新建查询");
        createQueryMenuItem.setOnAction((ActionEvent t) -> {
            System.out.println("新建查询 - 菜单点击");
            try {
                addTab("查询" + ApplicationStore.getCurrentNode().getData().toString(), new ImageView("/images/query.png"), QueryTabController.class, null);
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showException(e, rootPane);
            }
        });

        MenuItem createDbMenuItem = new MenuItem("新建数据库");
        createDbMenuItem.setOnAction((ActionEvent t) -> {
            createDbName.clear();
            createDbReplica.clear();
            createDbDays.clear();
            createDbBlocks.clear();
            System.out.println("新建数据库 - 菜单点击");
            createDbDialogTitle.setText("新建数据库");
            createDbName.setDisable(false);
            createDbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createDbDialog.show(rootPane);
        });

        MenuItem updateDbMenuItem = new MenuItem("编辑数据库");
        updateDbMenuItem.setOnAction((ActionEvent t) -> {
            createDbName.clear();
            createDbReplica.clear();
            createDbDays.clear();
            createDbBlocks.clear();
            System.out.println("编辑数据库 - 菜单点击");
            createDbDialogTitle.setText("编辑数据库");
            createDbName.setDisable(true);
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            //TODO 动态
            createDbName.setText(databaseModel.getDatabaseResDTO().get("name").toString());
            if(VersionUtils.compareVersion(databaseModel.getConnectionModel().getVersion(), "3.0") > 0) {
                createDbBlocks.setText(databaseModel.getDatabaseResDTO().get("buffer").toString());
            } else {
                createDbBlocks.setText(databaseModel.getDatabaseResDTO().get("blocks").toString());
            }

            createDbDays.setText(databaseModel.getDatabaseResDTO().get("keep").toString());
            createDbReplica.setText(databaseModel.getDatabaseResDTO().get("replica").toString());
            createDbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createDbDialog.show(rootPane);
        });


        MenuItem delDbMenuItem = new MenuItem("删除数据库");
        delDbMenuItem.setOnAction((ActionEvent t) -> {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            try {
                DataBaseUtils.deleteDatabase(TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel()),
                        databaseModel.getName());
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showException(e, rootPane);
            }
            for (TreeItem<CommonNode> treeItem : root.getChildren()) {
                if (treeItem.getValue().getName().equals(databaseModel.getConnectionModel().getName())) {
                    for (TreeItem<CommonNode> childTreeItem : treeItem.getChildren()) {
                        if (childTreeItem.getValue().getName().equals(databaseModel.getName())) {
                            treeItem.getChildren().remove(childTreeItem);
                            break;
                        }
                    }
                }
            }

        });


        MenuItem updateTbMenuItem = new MenuItem("编辑数据表");
        updateTbMenuItem.setOnAction((ActionEvent t) -> {
            System.out.println("编辑数据表 - 菜单点击");
            createTbDialogTitle.setText("编辑数据表");

            createTbPane.getChildren().remove(6, createTbPane.getChildren().size());
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            tableName0_TextField.setText(stableModel.getStb().get("name").toString());
            tableName0_TextField.setDisable(true);
            List<TableFieldDTO> fields = SuperTableUtils.getStableField(TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel()), stableModel.getDb().getName(), stableModel.getStb().get("name").toString());

            for (int i = 0; i < fields.size(); i++) {
                JFXTextField nameTextField = new JFXTextField();
                nameTextField.setId(nextRowIndex + "_NameField");
                nameTextField.setPadding(new Insets(0, 6, 0, 0));
                nameTextField.setText(fields.get(i).getName());
                nameTextField.setMinWidth(120);
                nameTextField.setDisable(true);
                createTbPane.add(nameTextField, 0, nextRowIndex);

                JFXComboBox<String> typeJFXComboBox = new JFXComboBox();
                typeJFXComboBox.setId(nextRowIndex + "_TypeField");
                typeJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
                typeJFXComboBox.setItems(JavaFxBeanUtils.getDataTypeObservableList());
                typeJFXComboBox.setValue(fields.get(i).getDataType());
                typeJFXComboBox.setMinWidth(120);
                createTbPane.add(typeJFXComboBox, 1, nextRowIndex);

                JFXTextField lengthTextField = new JFXTextField();
                lengthTextField.setId(nextRowIndex + "_LengthField");
                lengthTextField.setPadding(new Insets(0, 6, 0, 0));
                lengthTextField.setText(fields.get(i).getLength().toString());
                lengthTextField.setMinWidth(100);
                createTbPane.add(lengthTextField, 2, nextRowIndex);

                JFXComboBox<String> isTagJFXComboBox = new JFXComboBox<>();
                isTagJFXComboBox.setId(nextRowIndex + "_isTagField");
                isTagJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
                isTagJFXComboBox.setItems(JavaFxBeanUtils.getTrueFalseObservableList());
                isTagJFXComboBox.setValue(fields.get(i).getIsTag().toString());
                isTagJFXComboBox.setMinWidth(100);
                createTbPane.add(isTagJFXComboBox, 3, nextRowIndex);

                HBox hBox = new HBox();
                hBox.setId(nextRowIndex + "_hBox");
                hBox.setMinWidth(300);
                createTbPane.add(hBox, 4, nextRowIndex);

                JFXButton upButton = new JFXButton();
                upButton.setText("上移");
                upButton.setDisable(true);
                upButton.setId(nextRowIndex + "_upButton");

                JFXButton downButton = new JFXButton();
                downButton.setText("下移");
                downButton.setDisable(true);
                downButton.setId(nextRowIndex + "_downButton");

                JFXButton delButton = new JFXButton();
                delButton.setText("删除");
                delButton.setId(nextRowIndex + "_delButton");
                delButton.setOnAction((ActionEvent tt) -> {
                    System.out.println("删除 - 点击");
                    GridPaneUtils.deleteFieldRow(createTbPane, ((JFXButton) tt.getTarget()));
                    nextRowIndex--;
                });

                hBox.getChildren().addAll(upButton, downButton, delButton);
                nextRowIndex++;
            }


            createTbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createTbDialog.show(rootPane);
        });

        MenuItem createTbMenuItem = new MenuItem("新建数据表");
        createTbMenuItem.setOnAction((ActionEvent t) -> {
            System.out.println("新建数据表 - 菜单点击");
            createTbDialogTitle.setText("新建数据表");
            createTbPane.getChildren().remove(6, createTbPane.getChildren().size());
            createTbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createTbDialog.show(rootPane);
        });

        MenuItem delTbMenuItem = new MenuItem("删除数据表");
        delTbMenuItem.setOnAction((ActionEvent t) -> {
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            try {
                SuperTableUtils.deleteStable(TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel()),
                        stableModel.getDb().getName(), stableModel.getStb().get("name").toString());
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showException(e, rootPane);
            }
            for (TreeItem<CommonNode> treeItem : root.getChildren()) {
                if (treeItem.getValue().getName().equals(stableModel.getDb().getConnectionModel().getName())) {
                    for (TreeItem<CommonNode> childTreeItem : treeItem.getChildren()) {
                        if (childTreeItem.getValue().getName().equals(stableModel.getDb().getName())) {
                            for(TreeItem<CommonNode> stb:childTreeItem.getChildren()) {
                                if (stb.getValue().getName().equals(stableModel.getStb().get("name").toString())) {
                                    childTreeItem.getChildren().remove(stb);
                                    break;
                                }
                            }


                        }
                    }
                }
            }

        });


        // 创建右键菜单(连接、库、表)
        // 注册鼠标右击事件处理程序
        ContextMenu nodeClickMenu = new ContextMenu();
        leftTreeView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println(event);
            nodeClickMenu.getItems().clear();
            if (!event.getButton().equals(MouseButton.SECONDARY)) {
                return;
            }

//                Node node = event.getPickResult().getIntersectedNode();                //给node对象添加下来菜单；

            CommonNode clickNodeData = (leftTreeView.getSelectionModel().getSelectedItem()).getValue();
            if (clickNodeData.getType().equals(NodeTypeEnum.CONNECTION)) {
                nodeClickMenu.getItems().addAll(createQueryMenuItem, createConnectionMenuItem, updateConnectionMenuItem, deleteConnectionMenuItem, createDbMenuItem);
            } else if (clickNodeData.getType().equals(NodeTypeEnum.DB)) {
                nodeClickMenu.getItems().addAll(exportSQLMenuItem, createQueryMenuItem, createDbMenuItem, updateDbMenuItem, delDbMenuItem, createTbMenuItem);
            } else {
                nodeClickMenu.getItems().addAll(exportSQLMenuItem, createQueryMenuItem, createTbMenuItem, updateTbMenuItem, delTbMenuItem);
            }


            nodeClickMenu.show(leftTreeView, event.getScreenX(), event.getScreenY());
            System.out.println("Node click: " + clickNodeData);
        });

        // 监听当前的选择
        leftTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectTreeItem(newValue));

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setMinWidth(300);


    }

    private void onSelectTreeItem(TreeItem<CommonNode> treeItem) {
        try {
            CommonNode item = treeItem.getValue();
            ApplicationStore.setCurrentTreeItem(treeItem);
//            ApplicationStore.setCurrentNode(item);
            if (item.getType() == NodeTypeEnum.ROOT) {
                //do nothing
                log.info("click root node...");
                return;
            } else if (item.getType() == NodeTypeEnum.CONNECTION) {
                addTab(item.getData().toString(), ImageViewUtils.getImageViewByType(item.getType()), DbTabController.class, null);
            } else if (item.getType() == NodeTypeEnum.DB) {
                addTab(item.getData().toString(), ImageViewUtils.getImageViewByType(item.getType()), StbTabController.class, null);
            } else {
                addTab(item.getData().toString(), ImageViewUtils.getImageViewByType(item.getType()), RecordTabController.class, null);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            AlertUtils.showException(e, rootPane);
        }
    }

    private void showAddConnectionDialog() {
        nameTextField.clear();
        ipTextField.clear();
        portTextField.clear();
        usernameTextField.clear();
        passwordTextField.clear();
        if (!"新建连接".equals(dialogTitle.getText())) {
            try {
                ConnectionModel connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();
                List<Map<String, Object>> rst = H2DbUtils.query("select * from t_connection where name='" + connectionModel.getName() + "'");
                if (ObjectUtils.isNotEmpty(rst)) {
                    nameTextField.setText(rst.get(0).get("NAME").toString());
                    ipTextField.setText(rst.get(0).get("IP").toString());
                    portTextField.setText(rst.get(0).get("PORT").toString());
                    usernameTextField.setText(rst.get(0).get("USERNAME").toString());
                    passwordTextField.setText(rst.get(0).get("PASSWORD").toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showException(e, rootPane);
            }
        }


        dialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        dialog.show(rootPane);
    }

    public <T> void addTab(String title, Node icon, Class<T> controllerClass, Object userData) throws FlowException {
        FlowHandler flowHandler = new Flow(controllerClass).createHandler();
        Tab tab = ApplicationStore.getTabsMap().get(title);

        if (tab == null) {

            tab = new Tab(title);
            tab.setUserData(userData);
            tab.setGraphic(icon);

            try {
                StackPane node = flowHandler.start(new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.SWIPE_LEFT));
                node.getStyleClass().addAll("tab-content");
                tab.setContent(node);
            } catch (FlowException e) {
                e.printStackTrace();
                AlertUtils.showException(e, rootPane);
            }
            tabPane.getTabs().add(tab);
            ApplicationStore.getTabsMap().put(title, tab);
            tab.setOnClosed(event -> {
                ApplicationStore.getTabsMap().remove(title);
                try {
                    flowHandler.getCurrentViewContext().destroy();
                } catch (IllegalAccessException | InvocationTargetException e) {

                    log.error(e.getMessage(), e);
                    AlertUtils.showException(e, rootPane);
                }
            });
        }

        if ("主页".equals(title)) {
            tab.setClosable(false);
        }
        tabPane.getSelectionModel().select(tab);
    }

    protected void handExportDateAction(ActionEvent event) {
        // ShowDialog.showConfirmDialog(FXRobotHelper.getStages().get(0),
        // "是否导出数据到txt？", "信息");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.sql)", "*.sql");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = new Stage();
        File file = fileChooser.showSaveDialog(s);
        if (file == null)
            return;
        if (file.exists()) {//文件已存在，则删除覆盖文件
            file.delete();
        }
        String exportFilePath = file.getAbsolutePath();
        System.out.println("导出文件的路径" + exportFilePath);

        StringBuilder sql = new StringBuilder();
        if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.DB)) {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            String dbSql = DataBaseUtils.getDatabaseCreateSql(TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel()), databaseModel.getName());
            sql.append(dbSql + ";\n");
        } else if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.STB)) {
            StableModel databaseModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            String dbSql = SuperTableUtils.getStableSql(TsdbConnectionUtils.getConnection(databaseModel.getDb().getConnectionModel()), databaseModel.getDb().getName(), databaseModel.getStb().get("name").toString());
            sql.append(dbSql + ";\n");
        } else {
            //do nothing
        }


        FileUtil.writeString(sql.toString(), file, "utf-8");

        AlertUtils.show("导出成功!保存路径:\n" + exportFilePath);


    }

    @ActionMethod("saveConnection")
    private void saveConnection() throws SQLException {
        System.out.println("save Connection...");

        if ("新建连接".equals(dialogTitle.getText())) {


            ConnectionModel connectionModel = new ConnectionModel();
            connectionModel.setName(nameTextField.getText());
            connectionModel.setIp(ipTextField.getText());
            connectionModel.setPort(portTextField.getText());
            connectionModel.setUsername(usernameTextField.getText());
            connectionModel.setPassword(passwordTextField.getText());
            connectionModel.setVersion(RestConnectionUtils.getServerVersion(connectionModel));

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("name", nameTextField.getText());
            dataMap.put("ip", ipTextField.getText());
            dataMap.put("port", portTextField.getText());
            dataMap.put("username", usernameTextField.getText());
            dataMap.put("password", passwordTextField.getText());
            dataMap.put("version", connectionModel.getVersion());

            H2DbUtils.insertByHashMap("t_connection", Collections.singletonList(dataMap));
            root.getChildren().add(TreeUtils.generateConnectionTree(connectionModel));

        } else {
            ConnectionModel selectedConnectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();
            H2DbUtils.executeUpdate("update t_connection set name='" + nameTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set ip='" + ipTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set port='" + portTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set username='" + usernameTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set password='" + passwordTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            ApplicationStore.getCurrentNode().setName(nameTextField.getText());
            Event.fireEvent(ApplicationStore.getCurrentTreeItem(), new TreeItem.TreeModificationEvent<CommonNode>(TreeItem.valueChangedEvent(), ApplicationStore.getCurrentTreeItem(), ApplicationStore.getCurrentTreeItem().getValue()));
            selectedConnectionModel.setIp(ipTextField.getText());
            selectedConnectionModel.setPort(portTextField.getText());
            selectedConnectionModel.setUsername(usernameTextField.getText());
            selectedConnectionModel.setPassword(passwordTextField.getText());
            selectedConnectionModel.setVersion(RestConnectionUtils.getServerVersion(selectedConnectionModel));
        }
        dialog.close();
    }

    @ActionMethod("closeDialog")
    private void closeDialog() {
        dialog.close();
    }

    @ActionMethod("createDB")
    private void createDBSaveButton() {

        ConnectionModel connectionModel;

        if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.CONNECTION)) {
            connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();
        } else {
            connectionModel = ((DatabaseModel) ApplicationStore.getCurrentNode().getData()).getConnectionModel();
        }
        ConnectionDTO connection = TsdbConnectionUtils.getConnection(connectionModel);
        if ("新建数据库".equals(createDbDialogTitle.getText())) {
            DbConfigAddDTO dbConfigAddDTO = new DbConfigAddDTO();
            dbConfigAddDTO.setDbName(createDbName.getText());
            dbConfigAddDTO.setKeep(createDbDays.getText());
            dbConfigAddDTO.setBlocks(createDbBlocks.getText());
            dbConfigAddDTO.setReplica(createDbReplica.getText());
            DataBaseUtils.createDatabase(connection, dbConfigAddDTO);

            for (TreeItem<CommonNode> treeItem : root.getChildren()) {
                if (treeItem.getValue().getName().equals(connectionModel.getName())) {

                    DatabaseModel newDatabaseModel = new DatabaseModel(dbConfigAddDTO.getDbName(), DataBaseUtils.getDatabase(connection, dbConfigAddDTO.getDbName()), connectionModel);

                    TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(dbConfigAddDTO.getDbName(), NodeTypeEnum.DB, newDatabaseModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.DB));

                    treeItem.getChildren().add(dbNode);
                }
            }

        } else {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            DbConfigUpdateDTO dbConfigAddDTO = new DbConfigUpdateDTO();
            dbConfigAddDTO.setDbName(createDbName.getText());
//            dbConfigAddDTO.setDays(createDbDays.getText());
            dbConfigAddDTO.setBlocks(createDbBlocks.getText());
            dbConfigAddDTO.setReplica(createDbReplica.getText());
            DataBaseUtils.updateDatabase(connection, dbConfigAddDTO);

            ApplicationStore.getCurrentNode().setName(createDbName.getText());
            Event.fireEvent(ApplicationStore.getCurrentTreeItem(), new TreeItem.TreeModificationEvent<CommonNode>(TreeItem.valueChangedEvent(), ApplicationStore.getCurrentTreeItem(), ApplicationStore.getCurrentTreeItem().getValue()));
//TODO 动态
//            databaseModel.getDatabaseResDTO().setName(createDbName.getText());
//            databaseModel.getDatabaseResDTO().setComp(createDbComp.getText());
//            databaseModel.getDatabaseResDTO().setBlocks(createDbBlocks.getText());
//            databaseModel.getDatabaseResDTO().setReplica(createDbReplica.getText());
        }
        createDbDialog.close();
    }

    @ActionMethod("closeCreateDbDialog")
    private void closeCreateDbDialog() {
        createDbDialog.close();
    }

    @ActionMethod("closeCreateTbDialog")
    private void closeCreateTbDialog() {
        createTbDialog.close();
    }

    @ActionMethod("createTB")
    private void createTB() {
        DatabaseModel databaseModel;

        if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.DB)) {
            databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
        } else {
            databaseModel = ((StableModel) ApplicationStore.getCurrentNode().getData()).getDb();
        }
        ConnectionDTO connection = TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel());
        if ("新建数据表".equals(createTbDialogTitle.getText())) {


            StableAddDTO stableAddDTO = new StableAddDTO();
            stableAddDTO.setDb(databaseModel.getName());
            stableAddDTO.setTb(tableName0_TextField.getText());
            List<TableFieldDTO> fieldList = new ArrayList<>();
            stableAddDTO.setFieldList(fieldList);

            for (int i = 6; i < createTbPane.getChildren().size(); i = i + 5) {
                TableFieldDTO f = new TableFieldDTO();
                f.setName(((JFXTextField) createTbPane.getChildren().get(i)).getText());
                f.setDataType(((JFXComboBox) createTbPane.getChildren().get(i + 1)).getValue().toString());
                if (ObjectUtils.isNotEmpty(((JFXTextField) createTbPane.getChildren().get(i + 2)).getText())) {
                    f.setLength(Integer.valueOf(((JFXTextField) createTbPane.getChildren().get(i + 2)).getText()));
                }
                f.setIsTag(Boolean.valueOf(((JFXComboBox) createTbPane.getChildren().get(i + 3)).getValue().toString()));
                fieldList.add(f);
            }
            SuperTableUtils.createStable(connection, stableAddDTO);

            for (TreeItem<CommonNode> treeItem : root.getChildren()) {
                if (treeItem.getValue().getName().equals(databaseModel.getConnectionModel().getName())) {


                    for (TreeItem<CommonNode> treeTbItem : treeItem.getChildren()) {
                        if (treeTbItem.getValue().getName().equals(databaseModel.getName())) {


                            StableModel newDatabaseModel = new StableModel(SuperTableUtils.getStable(connection, stableAddDTO.getDb(), stableAddDTO.getTb()), databaseModel);

                            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(stableAddDTO.getTb(), NodeTypeEnum.STB, newDatabaseModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.STB));

                            treeTbItem.getChildren().add(dbNode);

                        }
                    }


                }
            }

        } else {

            List<TableFieldDTO> uiFieldList = new ArrayList<>();
            for (int i = 6; i < createTbPane.getChildren().size(); i = i + 5) {
                TableFieldDTO f = new TableFieldDTO();
                f.setName(((JFXTextField) createTbPane.getChildren().get(i)).getText());
                f.setDataType(((JFXComboBox) createTbPane.getChildren().get(i + 1)).getValue().toString());
                if (ObjectUtils.isNotEmpty(((JFXTextField) createTbPane.getChildren().get(i + 2)).getText())) {
                    f.setLength(Integer.valueOf(((JFXTextField) createTbPane.getChildren().get(i + 2)).getText()));
                }
                f.setIsTag(Boolean.valueOf(((JFXComboBox) createTbPane.getChildren().get(i + 3)).getValue().toString()));
                uiFieldList.add(f);
            }
            List<String> uiFieldNameList = uiFieldList.stream().map(TableFieldDTO::getName).collect(Collectors.toList());


            List<TableFieldDTO> tdFieldList = SuperTableUtils.getStableField(connection, databaseModel.getName(), tableName0_TextField.getText());
            List<String> tdFieldNameList = tdFieldList.stream().map(TableFieldDTO::getName).collect(Collectors.toList());


            List<TableFieldDTO> addList = uiFieldList.stream().filter(f -> !tdFieldNameList.contains(f.getName())).collect(Collectors.toList());
            List<TableFieldDTO> deleteList = tdFieldList.stream().filter(f -> !uiFieldNameList.contains(f.getName())).collect(Collectors.toList());
            List<TableFieldDTO> updateList = uiFieldList.stream().filter(
                    f -> {
                        for (TableFieldDTO fieldDTO : tdFieldList) {
                            if (fieldDTO.getName().equals(f.getName())) {
                                //对字段新增TAG
                                if (Boolean.TRUE.equals(f.getIsTag()) && Boolean.FALSE.equals(fieldDTO.getIsTag())) {
                                    addList.add(f);//新增TAG
                                    deleteList.add(fieldDTO);//删除字段
                                    return false;
                                }

                                if (!fieldDTO.getDataType().equals(f.getDataType())) {
                                    //类型改变
                                    return true;
                                }
                                if (("NCHAR".equals(f.getDataType()) || "BINARY".equals(f.getDataType())) && !fieldDTO.getLength().equals(f.getLength())) {
                                    //长度变更
                                    return true;
                                }
                                //无变化
                                break;
                            }
                        }
                        return false;
                    }
            ).collect(Collectors.toList());


            StableUpdateDTO stableUpdateDTO = new StableUpdateDTO();
            stableUpdateDTO.setDb(databaseModel.getName());
            stableUpdateDTO.setTb(tableName0_TextField.getText());
            stableUpdateDTO.setAddList(addList);
            stableUpdateDTO.setDeleteList(deleteList);
            stableUpdateDTO.setUpdateList(updateList);
            SuperTableUtils.updateStable(connection, stableUpdateDTO);

//            ApplicationStore.getCurrentNode().setName(createDbName.getText());
//            Event.fireEvent(ApplicationStore.getCurrentTreeItem(), new TreeItem.TreeModificationEvent<CommonNode>(TreeItem.valueChangedEvent(), ApplicationStore.getCurrentTreeItem(), ApplicationStore.getCurrentTreeItem().getValue()));
//            databaseModel.getDatabaseResDTO().setName(createDbName.getText());
//            databaseModel.getDatabaseResDTO().setComp(createDbComp.getText());
//            databaseModel.getDatabaseResDTO().setBlocks(createDbBlocks.getText());
//            databaseModel.getDatabaseResDTO().setReplica(createDbReplica.getText());
        }
        createTbDialog.close();
    }

    @ActionMethod("addField")
    private void addField() {
        JFXTextField nameTextField = new JFXTextField();
        nameTextField.setId(nextRowIndex + "_NameField");
        nameTextField.setPadding(new Insets(0, 6, 0, 0));
        nameTextField.setMinWidth(120);

        JFXComboBox<String> typeJFXComboBox = new JFXComboBox();
        typeJFXComboBox.setId(nextRowIndex + "_TypeField");
        typeJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
        typeJFXComboBox.setItems(JavaFxBeanUtils.getDataTypeObservableList());
        typeJFXComboBox.setMinWidth(120);
        typeJFXComboBox.setValue("TIMESTAMP");

        JFXTextField lengthTextField = new JFXTextField();
        lengthTextField.setId(nextRowIndex + "_LengthField");
        lengthTextField.setPadding(new Insets(0, 6, 0, 0));
        lengthTextField.setMinWidth(100);

        JFXComboBox<String> isTagJFXComboBox = new JFXComboBox<>();
        isTagJFXComboBox.setId(nextRowIndex + "_isTagField");
        isTagJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
        isTagJFXComboBox.setItems(JavaFxBeanUtils.getTrueFalseObservableList());
        isTagJFXComboBox.setMinWidth(100);
        isTagJFXComboBox.setValue("false");

        HBox hBox = new HBox();
        hBox.setId(nextRowIndex + "_hBox");
        hBox.setMinWidth(300);

        JFXButton upButton = new JFXButton();
        upButton.setText("上移");
        upButton.setDisable(true);
        upButton.setId(nextRowIndex + "_upButton");

        JFXButton downButton = new JFXButton();
        downButton.setText("下移");
        downButton.setDisable(true);
        downButton.setId(nextRowIndex + "_downButton");

        JFXButton delButton = new JFXButton();
        delButton.setText("删除");
        delButton.setId(nextRowIndex + "_delButton");
        delButton.setUserData(nextRowIndex);
        delButton.setOnAction((ActionEvent t) -> {
            System.out.println("删除 - 点击");
            GridPaneUtils.deleteFieldRow(createTbPane, ((JFXButton) t.getTarget()));
            nextRowIndex--;
        });

        hBox.getChildren().addAll(upButton, downButton, delButton);

        createTbPane.addRow(nextRowIndex, nameTextField, typeJFXComboBox, lengthTextField, isTagJFXComboBox, hBox);
        nextRowIndex++;
    }

    @ActionMethod("createQueryAction")
    public void createQueryAction() {
        try {
            addTab("数据查询", new ImageView("/images/query.png"), QueryTabController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showException(e, rootPane);
        }
    }
    @ActionMethod("aboutAction")
    public void aboutAction() {
        aboutDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        aboutDialog.show(rootPane);
    }

    @ActionMethod("createConnectionAction")
    private void createConnectionAction() {
        dialogTitle.setText("新建连接");
        showAddConnectionDialog();
    }

    @ActionMethod("exitAction")
    public void exitAction() {
        System.exit(0);
    }
    @ActionMethod("clusterQueryAction")
    public void clusterQueryAction() {
        try {
            addTab("集群查看", new ImageView("/images/cluster.png"), ClusterQueryController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showException(e, rootPane);
        }
    }
    @ActionMethod("userQueryAction")
    public void userQueryAction() {
        try {
            addTab("用户查看", new ImageView("/images/user_query.png"), UserQueryController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showException(e, rootPane);
        }
    }

    @ActionMethod("resourceMonitorAction")
    public void resourceMonitorAction() {


        if (ApplicationStore.getCurrentNode() == null) {
            AlertUtils.show("请先选择一个连接节点！");
            return;
        }

        try {
            addTab("监控" + ApplicationStore.getCurrentNode().getData().toString(), new ImageView("/images/monitor.png"), MonitorController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showException(e, rootPane);
        }
    }

    @ActionMethod("connectionMonitorAction")
    public void connectionMonitorAction() {
        try {
            addTab("连接监控", new ImageView("/images/connections.png"), ConnectionMonitorController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showException(e, rootPane);
        }
    }

    @ActionMethod("queryMonitorAction")
    public void queryMonitorAction() {
        try {
            addTab("查询监控", new ImageView("/images/query_monitor.png"), QueryMonitorController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showException(e, rootPane);
        }
    }

    @PreDestroy
    public void destroy() {
    }


}



