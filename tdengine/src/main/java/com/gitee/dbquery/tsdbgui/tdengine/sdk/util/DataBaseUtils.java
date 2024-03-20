package com.gitee.dbquery.tsdbgui.tdengine.sdk.util;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.db.DbConfigAddDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.db.DbConfigUpdateDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.DatabaseCreateResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.exception.DatabaseAlreadyExistException;
import com.gitee.dbquery.tsdbgui.tdengine.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据库工具类
 *
 * @author 风一样的码农
 * @since 2023/8/11
 **/
@Slf4j
public class DataBaseUtils {

    /**
     * 创建数据库
     *
     * @param connection     数据连接
     * @param dbConfigAddDTO 数据库配置
     */
    public static void createDatabase(ConnectionDTO connection, DbConfigAddDTO dbConfigAddDTO) {
        String createSql = "CREATE DATABASE " + dbConfigAddDTO.getDbName();
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getBlocks())) {
            if(VersionUtils.compareVersion(connection.getVersion(), "3.0") > 0) {
                createSql += " BUFFER " + dbConfigAddDTO.getBlocks();
            } else {
                createSql += " blocks " + dbConfigAddDTO.getBlocks();
            }

        }

        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getKeep())) {
            createSql += " keep " + dbConfigAddDTO.getKeep();
        }


        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getReplica())) {
            createSql += " replica " + dbConfigAddDTO.getReplica();
        }
        createSql = createSql + ";";

        try {
            RestConnectionUtils.executeUpdate(connection, Collections.singletonList(createSql));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("TDengine ERROR (381)")) {
                throw new DatabaseAlreadyExistException();
            }
            throw e;
        }
    }

    /**
     * 删除数据库，谨慎操作
     *
     * @param connection 连接
     * @param dbName     库名
     */
    public static void deleteDatabase(ConnectionDTO connection, String dbName) {
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList("DROP DATABASE IF EXISTS " + dbName + ";"));
    }

    /**
     * 获取数据库列表
     *
     * @param connection 连接
     * @return 数据库列表
     */
    public static QueryRstDTO getAllDatabase(ConnectionDTO connection) {
        if(VersionUtils.compareVersion(connection.getVersion(), "3.0") > 0) {
            return RestConnectionUtils.executeQuery(connection, "select  * from INFORMATION_SCHEMA.INS_DATABASES ;");
        }
        return RestConnectionUtils.executeQuery(connection, "show databases;");
    }

    /**
     * 获取数据库
     *
     * @param connection 连接
     * @param dbName     数据库名
     * @return 数据库
     */
    public static Map<String, Object> getDatabase(ConnectionDTO connection, String dbName) {
        QueryRstDTO allDb = getAllDatabase(connection);
        for (Map<String, Object> db : allDb.getDataList()) {
            if (db.get("name").equals(dbName)) {
                return db;
            }
        }
        return null;
    }

    /**
     * 获取数据库建表语句
     *
     * @param connection 连接
     * @param dbName     数据库名
     * @return CREATE语句
     */
    public static String getDatabaseCreateSql(ConnectionDTO connection, String dbName) {
        List<DatabaseCreateResDTO> list = RestConnectionUtils.executeQuery(connection, "SHOW CREATE DATABASE " + dbName + ";", DatabaseCreateResDTO.class);
        return list.get(0).getCreateDbSql();
    }

    private static boolean needUpdate(Object oldObj, Object newObj) {
        if (newObj == null) {
            return false;
        }
        return !newObj.equals(oldObj);
    }

    /**
     * 更新数据库
     *
     * @param connection        连接
     * @param dbConfigUpdateDTO 数据库配置
     */
    public static void updateDatabase(ConnectionDTO connection, DbConfigUpdateDTO dbConfigUpdateDTO) {
        Map<String, Object> databaseResDTO = getDatabase(connection, dbConfigUpdateDTO.getDbName());
        if (databaseResDTO == null) {
            throw new RuntimeException("database not exist...");
        }
        List<String> sqlList = new ArrayList<>();
//        if (needUpdate(databaseResDTO.getReplica(), dbConfigUpdateDTO.getReplica())) {
//            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " replica " + dbConfigUpdateDTO.getReplica() + ";");
//        }
//        if (needUpdate(databaseResDTO.getQuorum(), dbConfigUpdateDTO.getQuorum())) {
//            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " quorum " + dbConfigUpdateDTO.getQuorum() + ";");
//        }
//        if (needUpdate(databaseResDTO.getKeep(), dbConfigUpdateDTO.getKeep())) {
//            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " keep " + dbConfigUpdateDTO.getKeep() + ";");
//        }
//        if (needUpdate(databaseResDTO.getBlocks(), dbConfigUpdateDTO.getBlocks())) {
//            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " blocks " + dbConfigUpdateDTO.getBlocks() + ";");
//        }
//        if (needUpdate(databaseResDTO.getComp(), dbConfigUpdateDTO.getComp())) {
//            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " comp " + dbConfigUpdateDTO.getComp() + ";");
//        }
//        if (needUpdate(databaseResDTO.getCacheLast(), dbConfigUpdateDTO.getCacheLast())) {
//            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " cacheLast " + dbConfigUpdateDTO.getCacheLast() + ";");
//        }

        RestConnectionUtils.executeUpdate(connection, sqlList);
    }
}
