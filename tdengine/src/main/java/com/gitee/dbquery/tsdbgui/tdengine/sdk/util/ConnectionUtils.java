package com.gitee.dbquery.tsdbgui.tdengine.sdk.util;

import com.gitee.dbquery.tsdbgui.tdengine.sdk.annotation.TdField;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tsdbgui.tdengine.sdk.dto.res.SystemVariableResDTO;
import com.gitee.dbquery.tsdbgui.tdengine.util.ObjectUtils;
import com.google.common.base.CaseFormat;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * 数据连接工具类
 *
 * @author chenpi
 * @since 2021/12/2
 **/
@Log4j2
public class ConnectionUtils {
    public static String RS_URL = "jdbc:TAOS-RS://%s:%s?user=%s&password=%s&timezone=UTC-8&charset=UTF-8&locale=en_US.UTF-8";
    public static String RS_DB_URL = "jdbc:TAOS-RS://%s:%s/%s?user=%s&password=%s&timezone=UTC-8&charset=UTF-8&locale=en_US.UTF-8";
    public static String RS_DRIVER = "com.taosdata.jdbc.rs.RestfulDriver";

    /**
     * 关闭java.sql.Connection
     *
     * @param con java.sql.Connection
     */
    public static void close(Connection con) {
        close(con, null, null);
    }

    /**
     * 关闭java.sql.Statement
     *
     * @param st java.sql.Statement
     */
    public static void close(Statement st) {
        close(null, st, null);
    }

    /**
     * 关闭java.sql.Statement、java.sql.ResultSet
     *
     * @param st java.sql.Statement
     * @param rs java.sql.ResultSet
     */
    public static void close(Statement st, ResultSet rs) {
        close(null, st, rs);
    }

    /**
     * 关闭java.sql.Connection、java.sql.Statement、java.sql.ResultSet
     *
     * @param st  java.sql.Statement
     * @param con java.sql.Connection
     * @param rs  java.sql.ResultSet
     */
    public static void close(Connection con, Statement st, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("close ResultSet error...", e);
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                log.error("close Statement error...", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("close Connection error...", e);
            }
        }
    }

    /**
     * 执行查询操作
     * 注意Connection需要调用方手动关闭
     *
     * @param connection java.sql.Connection连接
     * @param sql        sql语句
     * @param clazz      类定义
     * @return 查询结果
     */
    public static <T> List<T> executeQuery(Connection connection, String sql, Class<T> clazz) {
        log.info("======> 执行 TD SQL： {}", sql);
        if (ObjectUtils.isEmpty(sql)) {
            return Collections.emptyList();
        }
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            Field[] fields = clazz.getDeclaredFields();
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                T obj = clazz.newInstance();
                for (Field field : fields) {
                    TdField fieldAnnotation = field.getAnnotation(TdField.class);
                    if (fieldAnnotation == null) {
                        continue;
                    }
                    String columnName = fieldAnnotation.value();
                    setValue(obj, columnName, field, rs);
                }
                list.add(obj);
            }
            return list;
        } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            log.error(e.toString(), e);
            log.error("======> 执行SQL失败， sql:{}, error：{}", sql, e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            com.gitee.dbquery.tsdbgui.tdengine.sdk.util.ConnectionUtils.close(statement, rs);
        }
    }

    /**
     * 执行查询操作
     * 注意Connection需要调用方手动关闭
     *
     * @param connection 连接
     * @param sql        SQL语句
     * @return 查询结果
     */
    public static QueryRstDTO executeQuery(Connection connection, String sql) {
        log.info("======> 执行 TD SQL： {}", sql);
        if (ObjectUtils.isEmpty(sql)) {
            return null;
        }
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            List<Map<String, Object>> list = new ArrayList<>();

            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            List<String> columnList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                columnList.add(md.getColumnName(i));
            }
            while (rs.next()) {
                Map<String, Object> obj = new HashMap<>();
                for (int i = 1; i <= count; i++) {
                    obj.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(obj);
            }

            return new QueryRstDTO(columnList, list);
        } catch (SQLException e) {
            log.error(e.toString(), e);
            log.error("======> 执行SQL失败， sql:{}, error：{}", sql, e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            com.gitee.dbquery.tsdbgui.tdengine.sdk.util.ConnectionUtils.close(statement, rs);
        }
    }

    /**
     * 执行更新操作
     * 注意Connection需要调用方手动关闭
     *
     * @param connection java.sql.Connection连接
     * @param batchSql   sql语句列表
     * @return an array of update counts containing one element for each
     * command in the batch.  The elements of the array are ordered according
     * to the order in which commands were added to the batch.
     */
    public static int[] executeUpdate(Connection connection, List<String> batchSql) {
        log.info("======> 执行 TD SQL： {}", batchSql);
        if (ObjectUtils.isEmpty(batchSql)) {
            return new int[]{0};
        }
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for (String sqlTmp : batchSql) {
                statement.addBatch(sqlTmp);
            }
            return statement.executeBatch();
        } catch (SQLException e) {
            log.error(e.toString(), e);
            log.error("======> 执行SQL失败， sql:{}, error：{}", batchSql, e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            com.gitee.dbquery.tsdbgui.tdengine.sdk.util.ConnectionUtils.close(statement);
        }
    }

    /**
     * 根据连接信息获取java.sql.Connection
     * 注意Connection需要调用方手动关闭
     *
     * @param connection 连接信息
     * @return java.sql.Connection连接
     */
    public static Connection getConnection(ConnectionDTO connection) {
        String jdbcUrl = String.format(RS_URL, connection.getIp(), connection.getRestfulPort(), connection.getUsername(), connection.getPassword());
        return getConnection(jdbcUrl);
    }

    /**
     * 获取Connection
     * 注意Connection需要调用方手动关闭
     *
     * @param connection 连接信息
     * @param db         数据库
     * @return java.sql.Connection连接
     */
    public static Connection getConnection(ConnectionDTO connection, String db) {
        String jdbcUrl = String.format(RS_DB_URL, connection.getIp(), connection.getRestfulPort(), db, connection.getUsername(), connection.getPassword());
        return getConnection(jdbcUrl);
    }

    private static Connection getConnection(String jdbcUrl) {
        try {
            Class.forName(RS_DRIVER);
            return DriverManager.getConnection(jdbcUrl);
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new RuntimeException("get td connection error.");
        }


//        Connection rsConnection;
//
//        ThreadPoolTaskExecutor executor = SpringContextUtils.getBean("threadPoolTaskExecutor");
//        Callable<Connection> work = () -> {
//            Class.forName(RS_DRIVER);
//            return DriverManager.getConnection(jdbcUrl);
//        };
//        Future<Connection> future = executor.submit(work);
//
//        try {
//            rsConnection = future.get(2, TimeUnit.SECONDS);
//        } catch (TimeoutException e) {
//            log.error(e.toString(), e);
//            throw new RuntimeException("获取数据库连接超时...请重试");
//        } catch (Exception e) {
//            log.error(e.toString(), e);
//            throw new RuntimeException("get td connection error.");
//        }
//        return rsConnection;

    }

    /**
     * 获取系统参数
     *
     * @param connection 连接
     * @return 系统参数
     */
    public static Map<String, String> getSystemVariable(Connection connection) {
        List<SystemVariableResDTO> list = com.gitee.dbquery.tsdbgui.tdengine.sdk.util.ConnectionUtils.executeQuery(connection, "SHOW VARIABLES;", SystemVariableResDTO.class);
        Map<String, String> map = new HashMap<>();
        list.forEach(nv -> map.put(nv.getName(), nv.getValue()));
        return map;
    }

    private static void setValue(Object obj, String columnName, Field field, ResultSet rs) throws NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException {
        String tdColumnName = ObjectUtils.isEmpty(columnName) ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()) : columnName;
        String fieldName = field.getName();
        String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method setMethod = obj.getClass().getMethod(setMethodName, field.getType());
        Class<?> fieldType = field.getType();
        if (fieldType.equals(String.class)) {
            setMethod.invoke(obj, rs.getString(tdColumnName));
        } else if (fieldType.equals(Double.class) || fieldType.equals(Double.TYPE)) {
            setMethod.invoke(obj, rs.getDouble(tdColumnName));
        } else if (fieldType.equals(Integer.class) || fieldType.equals(Integer.TYPE)) {
            setMethod.invoke(obj, rs.getInt(tdColumnName));
        } else if (fieldType.equals(Float.class) || fieldType.equals(Float.TYPE)) {
            setMethod.invoke(obj, rs.getFloat(tdColumnName));
        } else if (fieldType.equals(Long.class) || fieldType.equals(Long.TYPE)) {
            setMethod.invoke(obj, rs.getLong(tdColumnName));
        } else if (fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
            setMethod.invoke(obj, rs.getBoolean(tdColumnName));
        } else {
            setMethod.invoke(obj, rs.getObject(tdColumnName));
        }
    }
}
