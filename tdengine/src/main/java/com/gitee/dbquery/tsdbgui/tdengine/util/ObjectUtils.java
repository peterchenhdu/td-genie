package com.gitee.dbquery.tsdbgui.tdengine.util;


import cn.hutool.core.bean.BeanUtil;

/**
 * 对象工具类
 *
 * @author chenpi
 * @since 1.0.3
 **/
public class ObjectUtils extends BeanUtil {
ssss
    public static boolean isNotEmpty( Object[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty( Object obj) {
        return !isEmpty(obj);
    }


}
