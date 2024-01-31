package com.gitee.dbquery.tsdbgui.tdengine.store;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;

/**
 * ApplicationStore
 *
 * @author pc
 * @since 2024/01/31
 **/
public class ApplicationStore {

    public static String ICON_FONT_KEY = "icon.svg";
    private static SimpleBooleanProperty style = new SimpleBooleanProperty();

    private static MapProperty<String, String> featureMap;

    public static void clearPermissionInfo() {
        getFeatureMap().clear();
    }

    public static MapProperty<String, String> getFeatureMap() {
        if (featureMap == null) {
            ObservableMap<String, String> map = FXCollections.observableMap(new HashMap<>());
            featureMap = new SimpleMapProperty<>(map);
        }
        return featureMap;
    }

    public static boolean isStyle() {
        return style.get();
    }

    public static void setStyle(boolean style) {
        ApplicationStore.style.set(style);
    }

    public static SimpleBooleanProperty styleProperty() {
        return style;
    }


}


