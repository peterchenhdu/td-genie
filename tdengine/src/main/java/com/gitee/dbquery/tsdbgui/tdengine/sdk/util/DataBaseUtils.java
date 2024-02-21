package com.gitee.dbquery.tsdbgui.tdengine.sdk.util;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.db.DbConfigAddDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.db.DbConfigUpdateDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.DatabaseCreateResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.DatabaseResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.exception.DatabaseAlreadyExistException;
import com.gitee.dbquery.tsdbgui.tdengine.util.ObjectUtils;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库工具类
 *
 * @author chenpi
 * @since 2023/8/11
 **/
@Log4j2
public class DataBaseUtils {

    /**
     * 创建数据库
     *
     * @param connection     数据连接
     * @param dbConfigAddDTO 数据库配置
     */
    public static void createDatabase(Connection connection, DbConfigAddDTO dbConfigAddDTO) {
        String createSql = "CREATE DATABASE " + dbConfigAddDTO.getDbName();
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getBlocks())) {
            createSql += " blocks " + dbConfigAddDTO.getBlocks();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getCache())) {
            createSql += " cache " + dbConfigAddDTO.getCache();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getCacheLast())) {
            createSql += " cacheLast " + dbConfigAddDTO.getCacheLast();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getComp())) {
            createSql += " comp " + dbConfigAddDTO.getComp();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getDays())) {
            createSql += " days " + dbConfigAddDTO.getDays();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getFsync())) {
            createSql += " fsync " + dbConfigAddDTO.getFsync();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getKeep())) {
            createSql += " keep " + dbConfigAddDTO.getKeep();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getMaxRows())) {
            createSql += " maxRows " + dbConfigAddDTO.getMaxRows();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getMinRows())) {
            createSql += " minRows " + dbConfigAddDTO.getMinRows();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getPrecision())) {
            createSql += " precision '" + dbConfigAddDTO.getPrecision() + "'";
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getQuorum())) {
            createSql += " quorum " + dbConfigAddDTO.getQuorum();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getUpdate())) {
            createSql += " update " + dbConfigAddDTO.getUpdate();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getReplica())) {
            createSql += " replica " + dbConfigAddDTO.getReplica();
        }
        if (ObjectUtils.isNotEmpty(dbConfigAddDTO.getWalLevel())) {
            createSql += " wal " + dbConfigAddDTO.getWalLevel();
        }
        createSql = createSql + ";";

        try {
            ConnectionUtils.executeUpdate(connection, Collections.singletonList(createSql));
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
    public static void deleteDatabase(Connection connection, String dbName) {
        ConnectionUtils.executeUpdate(connection, Collections.singletonList("DROP DATABASE IF EXISTS " + dbName + ";"));
    }

    /**
     * 获取数据库列表
     *
     * @param connection 连接
     * @return 数据库列表
     */
    public static List<DatabaseResDTO> getAllDatabase(Connection connection) {
        return ConnectionUtils.executeQuery(connection, "show databases;", DatabaseResDTO.class);
    }

    /**
     * 获取数据库
     *
     * @param connection 连接
     * @param dbName     数据库名
     * @return 数据库
     */
    public static DatabaseResDTO getDatabase(Connection connection, String dbName) {
        List<DatabaseResDTO> allDb = getAllDatabase(connection);
        for (DatabaseResDTO db : allDb) {
            if (db.getName().equals(dbName)) {
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
    public static String getDatabaseCreateSql(Connection connection, String dbName) {
        List<DatabaseCreateResDTO> list = ConnectionUtils.executeQuery(connection, "SHOW CREATE DATABASE " + dbName + ";", DatabaseCreateResDTO.class);
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
    public static void updateDatabase(Connection connection, DbConfigUpdateDTO dbConfigUpdateDTO) {
        DatabaseResDTO databaseResDTO = getDatabase(connection, dbConfigUpdateDTO.getDbName());
        if (databaseResDTO == null) {
            throw new RuntimeException("database not exist...");
        }
        List<String> sqlList = new ArrayList<>();
        if (needUpdate(databaseResDTO.getReplica(), dbConfigUpdateDTO.getReplica())) {
            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " replica " + dbConfigUpdateDTO.getReplica() + ";");
        }
        if (needUpdate(databaseResDTO.getQuorum(), dbConfigUpdateDTO.getQuorum())) {
            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " quorum " + dbConfigUpdateDTO.getQuorum() + ";");
        }
        if (needUpdate(databaseResDTO.getKeep(), dbConfigUpdateDTO.getKeep())) {
            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " keep " + dbConfigUpdateDTO.getKeep() + ";");
        }
        if (needUpdate(databaseResDTO.getBlocks(), dbConfigUpdateDTO.getBlocks())) {
            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " blocks " + dbConfigUpdateDTO.getBlocks() + ";");
        }
        if (needUpdate(databaseResDTO.getComp(), dbConfigUpdateDTO.getComp())) {
            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " comp " + dbConfigUpdateDTO.getComp() + ";");
        }
        if (needUpdate(databaseResDTO.getCacheLast(), dbConfigUpdateDTO.getCacheLast())) {
            sqlList.add("ALTER DATABASE " + dbConfigUpdateDTO.getDbName() + " cacheLast " + dbConfigUpdateDTO.getCacheLast() + ";");
        }

        ConnectionUtils.executeUpdate(connection, sqlList);
    }
}
