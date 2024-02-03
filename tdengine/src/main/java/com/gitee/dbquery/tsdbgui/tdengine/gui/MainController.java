package com.gitee.dbquery.tsdbgui.tdengine.gui;

import com.gitee.dbquery.tsdbgui.tdengine.gui.component.CommonTabController;
import com.gitee.dbquery.tsdbgui.tdengine.model.CommonNode;
import com.gitee.dbquery.tsdbgui.tdengine.model.ConnectionModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.DatabaseModel;
import com.gitee.dbquery.tsdbgui.tdengine.model.TableModel;
import com.gitee.dbquery.tsdbgui.tdengine.store.H2DbUtils;
import com.gitee.dbquery.tsdbgui.tdengine.store.TsdbConnectionUtils;
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
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MainController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController("/fxml/main.fxml")
public class MainController {
    public static CommonNode currentNode;
    public static Connection currentConnection;
    private final HashMap<String, Tab> tabsMap = new HashMap<>();
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
    private MenuItem exitMenuItem;
    @FXML
    private JFXDialog dialog;
    @FXML
    @ActionTrigger("saveJob")
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


    private ImageView getImageViewByType(Integer type) {
        if(-1 == type) {
            return new ImageView("/images/logo.png");
        }
        String icon = type == 0 ? "tdengine.png" : type == 1 ? "db.png" : type == 2 ? "tb.png" : "";
        return new ImageView("/images/" + icon);
    }

    private void onSelectItem(CommonNode item) {
        if (item.getType() == -1) {
            return;
        }
        currentNode = item;
        try {
            addTab(item.getData().toString(), getImageViewByType(item.getType()), CommonTabController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showAddJobDialog() {
        dialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        dialog.show(rootPane);
    }

    @ActionMethod("closeDialog")
    private void closeDialog() {
        dialog.close();
    }

    @ActionMethod("saveJob")
    private void saveJob() throws SQLException {
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

    public void initTable() throws SQLException {
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

//
//        Map<String, Object> dataMap = new HashMap<>();
//        dataMap.put("id", 1L);
//        dataMap.put("name", "test");
//        dataMap.put("birthday", LocalDateTime.now());
//        dataMap.put("height", 12.6);
//        insertByHashMap("t_user", Collections.singletonList(dataMap));
//
//
//        List<Map<String, Object>> users = query("select * from  t_user;");
//        for (Map<String, Object> user : users) {
//            System.out.println(user);
//        }

    }

    private List<ConnectionModel> getConnectionList() throws SQLException {
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

    private TreeItem<CommonNode> getConnectionTreeItem(ConnectionModel connectionModel) {
        TreeItem<CommonNode> connectionItem = new TreeItem<>(new CommonNode(connectionModel.getName(), 0, connectionModel), getImageViewByType(0));


        Connection connection = TsdbConnectionUtils.getConnection(connectionModel);

        for (DatabaseResDTO db : DataBaseUtils.getAllDatabase(connection)) {
            List<StableResDTO> tbList = SuperTableUtils.getAllStable(connection, db.getName());
            DatabaseModel databaseModel = new DatabaseModel(db.getName(), db, connectionModel);
            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(db.getName(), 1, databaseModel), getImageViewByType(1));
            connectionItem.getChildren().add(dbNode);

            for (StableResDTO tb : tbList) {
                TreeItem<CommonNode> tbNode = new TreeItem<>(new CommonNode(tb.getName(), 2, new TableModel(tb, databaseModel)), getImageViewByType(2));
                dbNode.getChildren().add(tbNode);
            }
        }

        return connectionItem;
    }

    @PostConstruct
    public void init() throws SQLException {

        exitMenuItem.setOnAction((event)-> System.exit(0));

        initTable();

        splitPane.setDividerPositions(0.2);
        SplitPane.setResizableWithParent(leftTreeView, Boolean.FALSE);


        ApplicationContext.getInstance().register(this, MainController.class);


        createConnectionMenuItem.setOnAction((ActionEvent t) -> {
            System.out.println("菜单点击");
            showAddJobDialog();
        });


//        ConnectionDTO connectionDTO = new ConnectionDTO();
//        connectionDTO.setIp("10.162.201.62");
//        connectionDTO.setRestfulPort("6041");
//        connectionDTO.setUsername("root");
//        connectionDTO.setPassword("Abc123_");

//        ConnectionDTO connectionDTO = new ConnectionDTO();
//        connectionDTO.setIp("127.0.0.1");
//        connectionDTO.setRestfulPort("6041");
//        connectionDTO.setUsername("root");
//        connectionDTO.setPassword("taosdata");


//        Map<DatabaseResDTO, List<StableResDTO>> tbMap = new LinkedHashMap<>();
//        currentConnection = ConnectionUtils.getConnection(connectionDTO);
//        for (DatabaseResDTO db : DataBaseUtils.getAllDatabase(currentConnection)) {
//            List<StableResDTO> tbList = SuperTableUtils.getAllStable(currentConnection, db.getName());
//            tbMap.put(db, tbList);
//        }

        leftTreeView.setMinWidth(100);
        root = new TreeItem<>(new CommonNode("TSDB-GUI", -1, null), getImageViewByType(-1));
        root.setExpanded(true);
        leftTreeView.setRoot(root);
        leftTreeView.setShowRoot(false);

        List<ConnectionModel> connectionNodeList = getConnectionList();
        for (ConnectionModel connectionModel : connectionNodeList) {
            root.getChildren().add(getConnectionTreeItem(connectionModel));
        }

//
//        TreeItem<CommonNode> tdConnectionTreeItem = new TreeItem<>(new CommonNode("TD-127.0.0.1", 0, null));
//
//        root.getChildren().add(tdConnectionTreeItem);
//
//        tbMap.forEach((k, v) -> {
//            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(k.getName(), 1, k));
//            for (StableResDTO tb : v) {
//                TreeItem<CommonNode> tbNode = new TreeItem<>(new CommonNode(tb.getName(), 2, new TableModel(tb, k)));
//                dbNode.getChildren().add(tbNode);
//            }
//            tdConnectionTreeItem.getChildren().add(dbNode);
//        });


        // 监听当前的选择
        leftTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectItem(newValue.getValue()));

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setMinWidth(300);

    }


    public <T> void addTab(String title, Node icon, Class<T> controllerClass, Object userData) {
        FlowHandler flowHandler = new Flow(controllerClass).createHandler();
        Tab tab = tabsMap.get(title);

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
            tabsMap.put(title, tab);
            tab.setOnClosed(event -> {
                tabsMap.remove(title);
                try {
                    flowHandler.getCurrentViewContext().destroy();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }

        if ("主页".equals(title)) {
            tab.setClosable(false);
        }
        tabPane.getSelectionModel().select(tab);
    }


    @PreDestroy
    public void destroy() {
    }


}



