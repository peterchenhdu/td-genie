package com.gitee.dbquery.tdgenie.util;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author 风一样的码农
 * @since 2024/2/8
 **/
public class AlertUtils {
    public static void show(Node rootPane, String msg) {
        JFXAlert alert = new JFXAlert((Stage) rootPane.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("提示"));
        layout.setBody(new Label(msg));
        JFXButton closeButton = new JFXButton("确定");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);
        alert.show();
    }

    public static void showException(Throwable t, Node rootPane) {
        show(rootPane, "程序异常：" + t.getMessage());
    }
}
