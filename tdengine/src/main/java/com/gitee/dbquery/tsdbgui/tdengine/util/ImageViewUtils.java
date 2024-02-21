package com.gitee.dbquery.tsdbgui.tdengine.util;

import com.gitee.dbquery.tsdbgui.tdengine.common.enums.NodeTypeEnum;
import javafx.scene.image.ImageView;

/**
 * @author chenpi
 * @since 2024/2/6
 **/
public class ImageViewUtils {
    public static ImageView getImageViewByType(NodeTypeEnum nodeTypeEnum) {
        ImageView imageView;
        switch (nodeTypeEnum) {
            case ROOT:
                imageView = new ImageView("/images/logo.png");
                break;
            case CONNECTION:
                imageView = new ImageView("/images/tdengine.png");
                break;
            case DB:
                imageView = new ImageView("/images/db.png");
                break;
            case STB:
                imageView = new ImageView("/images/stb.png");
                break;
            case TB:
                imageView = new ImageView("/images/tb.png");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + nodeTypeEnum);
        }

        return imageView;
    }
}
