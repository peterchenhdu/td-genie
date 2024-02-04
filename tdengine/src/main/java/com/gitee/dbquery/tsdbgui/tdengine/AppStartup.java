package com.gitee.dbquery.tsdbgui.tdengine;

import com.gitee.dbquery.tsdbgui.tdengine.gui.MainController;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

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
public class AppStartup extends Application {

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;
    private static final String WINDOW_X_PROPERTY = "windowX";
    private static final String WINDOW_Y_PROPERTY = "windowY";
    private static final String WINDOW_WIDTH_PROPERTY = "windowWidth";
    private static final String WINDOW_HEIGHT_PROPERTY = "windowHeight";
    public static Double dividerPositions;
    private ApplicationContext applicationContext = ApplicationContext.getInstance();
    public static Properties sysConfigProperties = new Properties();
    static {
        try {
            sysConfigProperties.load(new FileInputStream(System.getProperty("user.home") + "/windowState.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void start(Stage stage) throws Exception {
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

        Scene scene = new Scene(wfxDecorator);
        stage.setTitle("TSDB-GUI");
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setX(Double.parseDouble(sysConfigProperties.getProperty(WINDOW_X_PROPERTY)));
        stage.setY(Double.parseDouble(sysConfigProperties.getProperty(WINDOW_Y_PROPERTY)));
        stage.setWidth(Double.parseDouble(sysConfigProperties.getProperty(WINDOW_WIDTH_PROPERTY)));
        stage.setHeight(Double.parseDouble(sysConfigProperties.getProperty(WINDOW_HEIGHT_PROPERTY)));
        stage.show();

        stage.setOnCloseRequest(event -> {
            System.out.print("监听到窗口关闭" + scene.getRoot());
            // 记录窗口位置和大小到本地存储
            Properties windowProperties = new Properties();
            windowProperties.put(WINDOW_X_PROPERTY, stage.getX() + "");
            windowProperties.put(WINDOW_Y_PROPERTY, stage.getY()+ "");
            windowProperties.put(WINDOW_WIDTH_PROPERTY, stage.getWidth()+ "");
            windowProperties.put(WINDOW_HEIGHT_PROPERTY, stage.getHeight()+ "");
            windowProperties.put("dividerPositions", dividerPositions+ "");
            try {
                windowProperties.store(new FileOutputStream(System.getProperty("user.home") + "/windowState.properties") , "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        /*全局样式*/
        scene.getStylesheets().addAll(AppStartup.class.getResource("/css/app.css").toExternalForm());

    }


    public static void main(String[] args) {
        launch(args);
    }
}
