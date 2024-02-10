package com.gitee.dbquery.tsdbgui.tdengine.gui;

import cn.hutool.core.io.FileUtil;
import com.gitee.dbquery.tsdbgui.tdengine.AppStartup;
import com.gitee.dbquery.tsdbgui.tdengine.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tsdbgui.tdengine.gui.component.*;
import com.gitee.dbquery.tsdbgui.tdengine.model.CommonNode;
import com.gitee.dbquery.tsdbgui.tdengine.model.ConnectionModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.DatabaseModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.StableModel;
import com.gitee.dbquery.tsdbgui.tdengine.store.ApplicationStore;
import com.gitee.dbquery.tsdbgui.tdengine.store.H2DbUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.TsdbConnectionUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.AlertUtils;
import com.gitee.dbquery.tsdbgui.tdengine.util.ImageViewUtils;
import com.jfoenix.controls.*;
import com.zhenergy.zntsdb.common.dto.res.DatabaseResDTO;
import com.zhenergy.zntsdb.common.dto.res.StableResDTO;
import com.zhenergy.zntsdb.common.util.DataBaseUtils;
import com.zhenergy.zntsdb.common.util.SuperTableUtils;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MainController
 *
 * @author pc
 * @since 2024/01/31
 **/
@Log4j2
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
    private MenuItem createConnectionMenuItem;
    @FXML
    private VBox createConnectionBox;
    @FXML
    private VBox queryBox;
    @FXML
    private VBox monitorBox;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    @ActionTrigger("aboutAction")
    private MenuItem aboutMenuItem;
    @FXML
    private JFXDialog dialog;
    @FXML
    private JFXDialog aboutDialog;
    @FXML
    @ActionTrigger("saveConnection")
    private JFXButton saveButton;
    @FXML
    @ActionTrigger("closeDialog")
    private JFXButton cancelButton;
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


    private TreeItem<CommonNode> root;


    private TreeItem<CommonNode> getConnectionTreeItem(ConnectionModel connectionModel) {
        TreeItem<CommonNode> connectionItem = new TreeItem<>(new CommonNode(connectionModel.getName(), NodeTypeEnum.CONNECTION, connectionModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.CONNECTION));


        Connection connection = TsdbConnectionUtils.getConnection(connectionModel);

        for (DatabaseResDTO db : DataBaseUtils.getAllDatabase(connection)) {
            List<StableResDTO> tbList = SuperTableUtils.getAllStable(connection, db.getName());
            DatabaseModel databaseModel = new DatabaseModel(db.getName(), db, connectionModel);
            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(db.getName(), NodeTypeEnum.DB, databaseModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.DB));
            connectionItem.getChildren().add(dbNode);

            for (StableResDTO tb : tbList) {
                TreeItem<CommonNode> tbNode = new TreeItem<>(new CommonNode(tb.getName(), NodeTypeEnum.STB, new StableModel(tb, databaseModel)), ImageViewUtils.getImageViewByType(NodeTypeEnum.STB));
                dbNode.getChildren().add(tbNode);
            }
        }

        return connectionItem;
    }


    @PostConstruct
    public void init() throws SQLException {
        aboutMenuItem.setOnAction((ActionEvent t) -> {
            aboutDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            aboutDialog.show(rootPane);
        });

        exitMenuItem.setOnAction((event) -> System.exit(0));

        ApplicationStore.connectionTbCheck();

        splitPane.getDividers().get(0).positionProperty().addListener(
                (o, oldPos, newPos) -> {
                    System.out.println(o);
                    AppStartup.dividerPositions = newPos.doubleValue();
                });


        ApplicationContext.getInstance().register(this, MainController.class);


        createConnectionMenuItem.setOnAction((ActionEvent t) -> {
            System.out.println("菜单点击");
            showAddConnectionDialog();
        });

        createConnectionBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("createConnectionBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            showAddConnectionDialog();
        });

        queryBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("queryBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            try {
                addTab("查询" + (ApplicationStore.getCurrentNode() == null ? System.currentTimeMillis() : ApplicationStore.getCurrentNode().getData().toString()), new ImageView("/images/query.png"), QueryTabController.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        monitorBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("queryBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            try {
                addTab("监控" + (ApplicationStore.getCurrentNode() == null ? System.currentTimeMillis() : ApplicationStore.getCurrentNode().getData().toString()), new ImageView("/images/query.png"), MonitorController.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        leftTreeView.setMinWidth(100);
        root = new TreeItem<>(new CommonNode("TSDB-GUI", NodeTypeEnum.ROOT, null), ImageViewUtils.getImageViewByType(NodeTypeEnum.ROOT));
        root.setExpanded(true);
        ApplicationStore.setConnectionTree(root);
        leftTreeView.setRoot(root);
        leftTreeView.setShowRoot(false);

        List<ConnectionModel> connectionNodeList = ApplicationStore.getConnectionList();
        for (ConnectionModel connectionModel : connectionNodeList) {
            root.getChildren().add(getConnectionTreeItem(connectionModel));
        }


        // 创建右键菜单(连接、库、表)
        ContextMenu dbMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("导出SQL");
        menuItem1.setOnAction((ActionEvent t) -> {
            handExportDateAction(t);
        });
        MenuItem menuItem2 = new MenuItem("新建查询");
        menuItem2.setOnAction((ActionEvent t) -> {
            System.out.println("新建查询 - 菜单点击");
            try {
                addTab("查询" + ApplicationStore.getCurrentNode().getData().toString(), new ImageView("/images/query.png"), QueryTabController.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        dbMenu.getItems().addAll(menuItem1, menuItem2);
        // 注册鼠标右击事件处理程序
        leftTreeView.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(event);
                dbMenu.hide();
                if (!event.getButton().equals(MouseButton.SECONDARY)) {
                    return;
                }

                Node node = event.getPickResult().getIntersectedNode();                //给node对象添加下来菜单；
                dbMenu.show(leftTreeView, event.getScreenX(), event.getScreenY());
                CommonNode name = (leftTreeView.getSelectionModel().getSelectedItem()).getValue();
                System.out.println("Node click: " + name);
            }
        });

        // 监听当前的选择
        leftTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectTreeItem(newValue.getValue()));

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setMinWidth(300);


    }

    private void onSelectTreeItem(CommonNode item) {
        try {
            ApplicationStore.setCurrentNode(item);
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
        }
    }

    private void showAddConnectionDialog() {
        dialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        dialog.show(rootPane);
    }


    public <T> void addTab(String title, Node icon, Class<T> controllerClass, Object userData) {
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
            }
            tabPane.getTabs().add(tab);
            ApplicationStore.getTabsMap().put(title, tab);
            tab.setOnClosed(event -> {
                ApplicationStore.getTabsMap().remove(title);
                try {
                    flowHandler.getCurrentViewContext().destroy();
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
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
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
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


        FileUtil.writeString(ApplicationStore.getCurrentNode().getData().toString(), file, "utf-8");

        AlertUtils.show(rootPane, "导出成功!保存路径:\n" + exportFilePath);


    }

    @ActionMethod("saveConnection")
    private void saveConnection() throws SQLException {
        System.out.println("save job...");

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", nameTextField.getText());
        dataMap.put("ip", ipTextField.getText());
        dataMap.put("port", portTextField.getText());
        dataMap.put("username", usernameTextField.getText());
        dataMap.put("password", passwordTextField.getText());

        H2DbUtils.insertByHashMap("t_connection", Collections.singletonList(dataMap));

        ConnectionModel connectionModel = new ConnectionModel();
        connectionModel.setName(nameTextField.getText());
        connectionModel.setIp(ipTextField.getText());
        connectionModel.setPort(portTextField.getText());
        connectionModel.setUsername(usernameTextField.getText());
        connectionModel.setPassword(passwordTextField.getText());

        root.getChildren().add(getConnectionTreeItem(connectionModel));
        System.out.println(ipTextField.getText());
        dialog.close();

    }

    @ActionMethod("closeDialog")
    private void closeDialog() {
        dialog.close();
    }

    @ActionMethod("aboutAction")
    public void about() {
        aboutDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        aboutDialog.show(rootPane);
    }

    @PreDestroy
    public void destroy() {
    }


}



