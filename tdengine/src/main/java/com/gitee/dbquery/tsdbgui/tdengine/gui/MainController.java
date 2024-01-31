package com.gitee.dbquery.tsdbgui.tdengine.gui;

import com.gitee.dbquery.tsdbgui.tdengine.gui.component.CommonTabController;
import com.gitee.dbquery.tsdbgui.tdengine.model.CommonNode;
import com.gitee.dbquery.tsdbgui.tdengine.model.TableModel;
import com.gitee.dbquery.tsdbgui.tdengine.store.ApplicationStore;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTreeView;
import com.jfoenix.svg.SVGGlyphLoader;
import com.zhenergy.zntsdb.common.dto.ConnectionDTO;
import com.zhenergy.zntsdb.common.dto.res.DatabaseResDTO;
import com.zhenergy.zntsdb.common.dto.res.StableResDTO;
import com.zhenergy.zntsdb.common.util.ConnectionUtils;
import com.zhenergy.zntsdb.common.util.DataBaseUtils;
import com.zhenergy.zntsdb.common.util.SuperTableUtils;
import io.datafx.controller.ViewController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.AnimatedFlowContainer;
import io.datafx.controller.flow.container.ContainerAnimations;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private SplitPane splitPane;
    @FXML
    private JFXTreeView<CommonNode> leftTreeView;
    @FXML
    private JFXTabPane tabPane;

    private void onSelectItem(CommonNode item) {
        if (item.getType() == -1) {
            return;
        }
        currentNode = item;
        System.out.println(item);
        try {
            String tabPrefix = item.getType() == 0 ? "连接" : item.getType() == 1 ? "库" : item.getType() == 2 ? "表" : "";
            String icon = item.getType() == 0 ? "connection" : item.getType() == 1 ? "database" : item.getType() == 2 ? "stable" : "";
            addTab(item.getName(), SVGGlyphLoader.getIcoMoonGlyph(ApplicationStore.ICON_FONT_KEY + "." + icon), CommonTabController.class, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @PostConstruct
    public void init() {

        splitPane.setDividerPositions(0.25, 1);

        ApplicationContext.getInstance().register(this, MainController.class);


//        ConnectionDTO connectionDTO = new ConnectionDTO();
//        connectionDTO.setIp("10.162.201.62");
//        connectionDTO.setRestfulPort("6041");
//        connectionDTO.setUsername("root");
//        connectionDTO.setPassword("Abc123_");

        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setIp("127.0.0.1");
        connectionDTO.setRestfulPort("6041");
        connectionDTO.setUsername("root");
        connectionDTO.setPassword("taosdata");


        Map<DatabaseResDTO, List<StableResDTO>> tbMap = new LinkedHashMap<>();
        currentConnection = ConnectionUtils.getConnection(connectionDTO);
        for (DatabaseResDTO db : DataBaseUtils.getAllDatabase(currentConnection)) {
            List<StableResDTO> tbList = SuperTableUtils.getAllStable(currentConnection, db.getName());
            tbMap.put(db, tbList);
        }

        leftTreeView.setMinWidth(100);
        TreeItem<CommonNode> root = new TreeItem<>(new CommonNode("根节点", -1, null));
        root.setExpanded(true);
        leftTreeView.setRoot(root);


        TreeItem<CommonNode> tdConnectionTreeItem = new TreeItem<>(new CommonNode("TD-127.0.0.1", 0, null));

        root.getChildren().add(tdConnectionTreeItem);

        tbMap.forEach((k, v) -> {
            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(k.getName(), 1, k));
            for (StableResDTO tb : v) {
                TreeItem<CommonNode> tbNode = new TreeItem<>(new CommonNode(tb.getName(), 2, new TableModel(tb, k)));
                dbNode.getChildren().add(tbNode);
            }
            tdConnectionTreeItem.getChildren().add(dbNode);
        });


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



