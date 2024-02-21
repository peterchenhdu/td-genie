package com.gitee.dbquery.tsdbgui.tdengine.sdk.util;

import com.gitee.dbquery.tsdbgui.tdengine.util.ObjectUtils;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.StableCreateResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.StableResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.TableFieldResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.stb.StableAddDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.stb.StableUpdateDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.exception.TableAlreadyExistException;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.util.ConnectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 超级表工具类
 *
 * @author chenpi
 * @since 2023/8/18
 **/
public class SuperTableUtils {

    /**
     * 创建超级表
     *
     * @param connection   连接
     * @param stableAddDTO 建表信息
     */
    public static void createStable(Connection connection, StableAddDTO stableAddDTO) {
        List<TableFieldDTO> fieldList = stableAddDTO.getFieldList().stream().filter(f -> !f.getIsTag()).collect(Collectors.toList());
        List<TableFieldDTO> tagList = stableAddDTO.getFieldList().stream().filter(TableFieldDTO::getIsTag).collect(Collectors.toList());

        StringBuilder sql = new StringBuilder("create " + (ObjectUtils.isEmpty(tagList) ? "TABLE " : "STABLE ") + stableAddDTO.getDb() + ".");
        sql.append(stableAddDTO.getTb()).append(" (");
        fieldList.forEach(field -> {
            sql.append(field.getName()).append(" ").append(getFieldType(field)).append(",");
        });

        if (fieldList.size() > 0) {
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
        }


        if (ObjectUtils.isNotEmpty(tagList)) {
            sql.append(" tags (");
            tagList.forEach(tag -> {
                sql.append(tag.getName()).append(" ").append(getFieldType(tag)).append(",");
            });

            if (tagList.size() > 0) {
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(")");
            }
        }

        sql.append(";");

        try {
            ConnectionUtils.executeUpdate(connection, Collections.singletonList(sql.toString()));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("TDengine ERROR (360)")) {
                throw new TableAlreadyExistException();
            }
            throw e;
        }
    }

    /**
     * 删除超级表，谨慎操作
     *
     * @param connection 连接
     * @param dbName     库名
     * @param tbName     表名
     */
    public static void deleteStable(Connection connection, String dbName, String tbName) {
        ConnectionUtils.executeUpdate(connection, Collections.singletonList("DROP STABLE IF EXISTS " + dbName + "." + tbName + ";"));
    }

    /**
     * 获取所有超级表
     *
     * @param connection 连接
     * @param db         库
     * @return 超级表列表
     */
    public static List<StableResDTO> getAllStable(Connection connection, String db) {
        return ConnectionUtils.executeQuery(connection, "show " + db + ".stables;", StableResDTO.class);
    }

    /**
     * 生成字段类型
     *
     * @param field 字段
     * @return 类型+长度
     */
    private static String getFieldType(TableFieldDTO field) {
        if (ObjectUtils.isEmpty(field.getLength())) {
            return field.getDataType();
        } else {
            return field.getDataType() + "(" + field.getLength() + ")";
        }
    }

    /**
     * 获取单个超级表
     *
     * @param connection 连接
     * @param db         库
     * @param stb        超级表
     * @return 超级表
     */
    public static StableResDTO getStable(Connection connection, String db, String stb) {
        List<StableResDTO> allStb = getAllStable(connection, db);
        for (StableResDTO stbTmp : allStb) {
            if (stbTmp.getName().equals(stb)) {
                return stbTmp;
            }
        }
        return null;
    }

    /**
     * 获取超级表字段信息
     *
     * @param connection 连接
     * @param dbName     库
     * @param tbName     表
     * @return 字段列表
     */
    public static List<TableFieldDTO> getStableField(Connection connection, String dbName, String tbName) {
        List<TableFieldResDTO> fieldList = ConnectionUtils.executeQuery(connection, "DESCRIBE " + dbName + "." + tbName + ";", TableFieldResDTO.class);

        return fieldList.stream().map(f -> {
            TableFieldDTO tf = new TableFieldDTO();
            tf.setLength(f.getLength());
            tf.setDataType(f.getType());
            tf.setName(f.getField());
            tf.setIsTag("TAG".equals(f.getNote()));
            return tf;
        }).collect(Collectors.toList());
    }

    /**
     * 获取建表语句
     *
     * @param connection 连接
     * @param dbName     库
     * @param stb        超级表
     * @return SQL
     */
    public static String getStableSql(Connection connection, String dbName, String stb) {
        List<StableCreateResDTO> list = ConnectionUtils.executeQuery(connection, "SHOW CREATE STABLE  " + dbName + "." + stb + ";", StableCreateResDTO.class);
        return list.get(0).getCreateDbSql();
    }

    /**
     * 修改超级表
     *
     * @param connection      连接
     * @param stableUpdateDTO 更新信息
     */
    public static void updateStable(Connection connection, StableUpdateDTO stableUpdateDTO) {
        List<String> batchSql = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getAddList())) {
            stableUpdateDTO.getAddList().forEach(f -> batchSql.add("ALTER STABLE " + stableUpdateDTO.getDb() + "." + stableUpdateDTO.getTb() + " ADD " + (f.getIsTag() ? "TAG " : "COLUMN ") + f.getName() + " " + getFieldType(f) + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getDeleteList())) {
            stableUpdateDTO.getDeleteList().forEach(f -> batchSql.add("ALTER STABLE " + stableUpdateDTO.getDb() + "." + stableUpdateDTO.getTb() + " DROP " + (f.getIsTag() ? "TAG " : "COLUMN ") + f.getName() + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getUpdateList())) {
            stableUpdateDTO.getUpdateList().forEach(f -> batchSql.add("ALTER STABLE " + stableUpdateDTO.getDb() + "." + stableUpdateDTO.getTb() + " MODIFY  " + (f.getIsTag() ? "TAG " : "COLUMN ") + f.getName() + " " + getFieldType(f) + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getTagNameChangeMap())) {
            stableUpdateDTO.getTagNameChangeMap().forEach((k, v) -> batchSql.add("ALTER STABLE " + stableUpdateDTO.getDb() + "." + stableUpdateDTO.getTb() + " CHANGE  TAG " + k + " " + v + ";"));
        }

        ConnectionUtils.executeUpdate(connection, batchSql);
    }
}
