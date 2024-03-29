package com.gitee.dbquery.tdgenie;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TabPaneWithScrollBarExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        // 动态添加多个标签页
        for (int i = 1; i <= 20; i++) {
            Tab tab = new Tab("Tab " + i, new ScrollPane(new StackPane()));
            tabPane.getTabs().add(tab);
        }

        // 设置ScrollPane属性以创建下拉列表
        ScrollPane scrollPane = new ScrollPane(tabPane);
        scrollPane.setFitToWidth(true); // 宽度适应内容
        scrollPane.setPrefSize(200, 200); // 设置首选大小

        Scene scene = new Scene(scrollPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
