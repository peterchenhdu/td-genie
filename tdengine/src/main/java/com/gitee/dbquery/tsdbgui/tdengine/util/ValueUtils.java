package com.gitee.dbquery.tsdbgui.tdengine.util;

/**
 * @author chenpi
 * @since 1.0.0 2024/2/4 21:11
 **/
public class ValueUtils {
    public static String getString(String obj, Object defaultValue) {
        return obj == null ? defaultValue.toString():obj;
    }
}
