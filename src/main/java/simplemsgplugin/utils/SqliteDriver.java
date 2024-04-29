package simplemsgplugin.utils;

import java.sql.*;
import java.util.*;


public class SqliteDriver {
    public final Connection connection;

    private static StringBuilder[] buildSqliteGroups(Map<String, Object> map) {
        StringBuilder fields = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        String lastKey = null;
        for (String key : map.keySet()) {
            lastKey = key;
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            fields.append("'").append(key).append("'");
            if (value instanceof String || value instanceof UUID) {
                values.append("'").append(value).append("'");
            } else {
                values.append(value);
            }

            if (!key.contains(lastKey)) {
                fields.append(", ");
                values.append(", ");
            }

        }
        fields.append(")");
        values.append(")");
        return new StringBuilder[]{fields, values};
    }

    private static List<Map<String, Object>> parseResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> resultList = new ArrayList<>();

        while (resultSet.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                resultMap.put(columnName, value);
            }
            resultList.add(resultMap);
        }
        return resultList;
    }

    public SqliteDriver(String fileName) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        createDatabase();
    }

    public void createDatabase() throws SQLException {
        Statement cursor = connection.createStatement();
        cursor.execute("CREATE TABLE IF NOT EXISTS SOUNDS (UUID TEXT NOT NULL PRIMARY KEY, Sound TEXT);");
        cursor.execute("CREATE TABLE IF NOT EXISTS BLACKLIST (UUID TEXT, BlockedUUID TEXT, BlockedPlayer TEXT);");
        cursor.close();
    }

    public List<Map<String, Object>> sqlSelectData(String fields, String table, String condition) throws SQLException {
        Statement cursor = connection.createStatement();
        ResultSet resultSet = cursor.executeQuery(String.format("SELECT %s FROM '%s' WHERE %s;", fields, table, condition));
        List<Map<String, Object>> resultList = parseResultSet(resultSet);
        resultSet.close();
        cursor.close();
        return resultList;
    }

    public List<Map<String, Object>> sqlSelectData(String fields, String table) throws SQLException {
        Statement cursor = connection.createStatement();
        ResultSet resultSet = cursor.executeQuery(String.format("SELECT %s FROM '%s';", fields, table));
        List<Map<String, Object>> resultList = parseResultSet(resultSet);
        resultSet.close();
        cursor.close();
        return resultList;
    }

    public List<Map<String, Object>> sqlSelectData(String fields, String table, String field, Integer limit) throws SQLException {
        Statement cursor = connection.createStatement();
        ResultSet resultSet = cursor.executeQuery(String.format("SELECT %s FROM '%s' ORDER BY %s DESC LIMIT %s;", fields, table, field, limit));
        List<Map<String, Object>> resultList = parseResultSet(resultSet);
        resultSet.close();
        cursor.close();
        return resultList;
    }

    public void sqlUpdateData(String table, String settable, String condition) throws SQLException {
        Statement cursor = connection.createStatement();
        cursor.execute(String.format("UPDATE '%s' SET %s WHERE %s;", table, settable, condition));
        cursor.close();
    }

    public void sqlInsertData(String table, Map<String, Object> datas) throws SQLException {
        Statement cursor = connection.createStatement();
        StringBuilder[] buildData = buildSqliteGroups(datas);
        cursor.execute(String.format("INSERT INTO '%s' %s VALUES %s;", table, buildData[0], buildData[1]));
        cursor.close();
    }

    public void sqlDeleteData(String table, String condition) throws SQLException {
        Statement cursor = connection.createStatement();
        cursor.execute(String.format("DELETE FROM '%s' WHERE %s;", table, condition));
        cursor.close();
    }
}