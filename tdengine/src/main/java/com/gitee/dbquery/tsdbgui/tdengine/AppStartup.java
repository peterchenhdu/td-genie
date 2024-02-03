package com.gitee.dbquery.tsdbgui.tdengine;

import com.gitee.dbquery.tsdbgui.tdengine.gui.MainController;
import com.gitee.dbquery.tsdbgui.tdengine.store.ApplicationStore;
import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
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

import java.io.IOException;


/**
 * 启动类
 *
 * @author pc
 * @since 2024/01/31
 **/
public class AppStartup extends Application {

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;

    private ApplicationContext applicationContext = ApplicationContext.getInstance();

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

        Scene scene = new Scene(wfxDecorator, 1200, 1000);
        stage.setTitle("TSDB-GUI");
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        /*全局样式*/
        scene.getStylesheets().addAll(AppStartup.class.getResource("/css/app.css").toExternalForm());

    }


    public static void main(String[] args) {
        launch(args);
    }
}
