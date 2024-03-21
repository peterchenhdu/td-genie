package com.gitee.dbquery.tdgenie;

import com.gitee.dbquery.tdgenie.gui.MainController;
import com.gitee.dbquery.tdgenie.util.ValueUtils;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.AnimatedFlowContainer;
import io.datafx.controller.flow.container.ContainerAnimations;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * 启动类
 *
 * @author pc
 * @since 2024/01/31
 **/
@Slf4j
public class AppStartup extends Application {

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;
    private static final String WINDOW_X_PROPERTY = "windowX";
    private static final String WINDOW_Y_PROPERTY = "windowY";
    private static final String WINDOW_WIDTH_PROPERTY = "windowWidth";
    private static final String WINDOW_HEIGHT_PROPERTY = "windowHeight";
    public static Double dividerPositions;
    public static Scene scene;
    private ApplicationContext applicationContext = ApplicationContext.getInstance();
    public static Properties sysConfigProperties = new Properties();

    static {
        try {
            sysConfigProperties.load(new FileInputStream(System.getProperty("user.home") + "/windowState.properties"));
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

    }


    @Override
    public void start(Stage stage) throws Exception {
        // Image 的 url 是 classpath 的相对目录，也可以是一个绝对目录
        Image image = new Image("/images/logo.png");
        stage.getIcons().add(image);

        Flow contentFlow = new Flow(MainController.class);
        AnimatedFlowContainer container = new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.SWIPE_LEFT);
        flowContext = new ViewFlowContext();
        final FlowHandler contentFlowHandler = contentFlow.createHandler(flowContext);
        applicationContext.register(stage, Stage.class);
        applicationContext.register("ContentFlowHandler", contentFlowHandler);
        contentFlowHandler.start(container);

        JFXDecorator wfxDecorator = new JFXDecorator(stage, container.getView());
        wfxDecorator.setCustomMaximize(true);
        wfxDecorator.setGraphic(new SVGGlyph(""));

        scene = new Scene(wfxDecorator);
        stage.setTitle("td-genie");
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setX(Double.parseDouble(ValueUtils.getString(sysConfigProperties.getProperty(WINDOW_X_PROPERTY), 100)));
        stage.setY(Double.parseDouble(ValueUtils.getString(sysConfigProperties.getProperty(WINDOW_Y_PROPERTY), 100)));
        stage.setWidth(Double.parseDouble(ValueUtils.getString(sysConfigProperties.getProperty(WINDOW_WIDTH_PROPERTY), 800)));
        stage.setHeight(Double.parseDouble(ValueUtils.getString(sysConfigProperties.getProperty(WINDOW_HEIGHT_PROPERTY), 600)));
        stage.show();
        stage.setOnCloseRequest(event -> {
            System.out.print("监听到窗口关闭" + scene.getRoot());
            // 记录窗口位置和大小到本地存储
            Properties windowProperties = new Properties();
            windowProperties.put(WINDOW_X_PROPERTY, stage.getX() + "");
            windowProperties.put(WINDOW_Y_PROPERTY, stage.getY() + "");
            windowProperties.put(WINDOW_WIDTH_PROPERTY, stage.getWidth() + "");
            windowProperties.put(WINDOW_HEIGHT_PROPERTY, stage.getHeight() + "");
            windowProperties.put("dividerPositions", dividerPositions + "");
            try {
                windowProperties.store(new FileOutputStream(System.getProperty("user.home") + "/windowState.properties"), "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        /*全局样式*/
        scene.getStylesheets().addAll(AppStartup.class.getResource("/css/app.css").toExternalForm());
        scene.getStylesheets().addAll(AppStartup.class.getResource("/css/sql-keyword.css").toExternalForm());
        System.out.println(Double.parseDouble(ValueUtils.getString(AppStartup.sysConfigProperties.getProperty("dividerPositions"), 0.2)));
        ((SplitPane) (scene.lookup("#splitPane"))).setDividerPositions(Double.parseDouble(ValueUtils.getString(AppStartup.sysConfigProperties.getProperty("dividerPositions"), 0.2)));

        com.exe4j.Controller.hide();
        System.out.println("hide...");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
