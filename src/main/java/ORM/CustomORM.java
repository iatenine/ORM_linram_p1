package ORM;

import logging.ORMLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static ORM.HelperOrm.buildColumn;
import static ORM.HelperOrm.executeQuery;
import static ORM.HelperOrm.buildValues;

public class CustomORM{

    static Connection conn = null;

    public static boolean connect(){
        conn = JDBCConnection.getConnection();
        return conn != null;
    }

    public static boolean connect(String endpoint, String username, String password) {
            try{
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                Connection testConn = DriverManager.getConnection(url, username, password);
                if(testConn != null)
                    conn = testConn;
                return (conn instanceof JDBCConnection);
            } catch (SQLException e){
                ORMLogger.logger.error(e.getSQLState());
                for (StackTraceElement elem : e.getStackTrace()) {
                    ORMLogger.logger.info(elem);
                }
                return false;
            }
    }

    public static String buildTable(String tableName, HashMap<String, Class> columns) {
        // Protect against errors caused by spaces and eliminate all UPPERCASE..age
        tableName = tableName.replaceAll(" ", "_").toLowerCase(Locale.ROOT);
        String str = buildColumn(columns);
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "( id serial primary key "
                + str
                + ")";
        HelperOrm.executeStatement(conn, sql);
        return tableName;
    }

    public static void dropTable(String tableName){
        String sql = "Drop Table if exists " + tableName + ";";
        HelperOrm.executeStatement(conn, sql);
    }

    public static int addRow(String tableName, Object ...entries){
        ResultSet rs;
        String sql = "Insert Into " + tableName + " Values " + buildValues(entries);
        rs = HelperOrm.executeQuery(conn, sql);
        try {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

    }

    public static ResultSet getJoinedTables(String firstTable, String foreignTable, String[] colNames, String condition){
        return null;
    }

    public static ResultSet getRow(String tableName, int id, String[] colNames){
        String sql = HelperOrm.selectStatementBuilder(tableName, colNames) + " WHERE id=" + id;
        return executeQuery(conn, sql);
    }

    public static ResultSet getRows(String tableName, String[] colNames){
        String sql = HelperOrm.selectStatementBuilder(tableName, colNames);
        return executeQuery(conn, sql);
    }

    // update row
    public static ResultSet updateRow(String tableName, int id, HashMap<String, Object> newEntries){
        //UPDATE tableName SET colName=newValue WHERE id=target RETURNING *;
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(tableName);
        sb.append(" SET ");
        for(String colName : newEntries.keySet()){
            sb.append(HelperOrm.sanitizeName(colName));
            sb.append("=");
            sb.append(HelperOrm.formatValues(newEntries.get(colName)));
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(" WHERE id=");
        sb.append(id);
        sb.append(" RETURNING *");
        return HelperOrm.executeQuery(conn, sb.toString());
    }

    // delete row
    public static ResultSet deleteRow(String tableName, int id){

        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM " + tableName + " WHERE id = " + id + ";";

            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int count = 0;
            while(rs.next()){
                count++;
            }
            if(count == 1){
                String query = "DELETE FROM " + tableName + " WHERE id = " + id + " RETURNING *;";
                return HelperOrm.executeQuery(conn, query);

            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static ResultSet getJoin(String tableName1, String tableName2, String[] colNames){
        StringBuilder sql = new StringBuilder("Select ");
        for(int i = 0; i < colNames.length; i ++) {
            sql.append(HelperOrm.sanitizeName(colNames[i]));
            if(i == colNames.length-1){
                sql.append(" ");
            } else {
                sql.append(", ");
            }
        }
        sql.append("From ");
        sql.append(tableName1);
        sql.append(" Natural Join ");
        sql.append(tableName2);

        return executeQuery(conn, sql.toString());
    }

    // Create Foreign Keys
    public static void addForeignKey(String tableName, String foreignTable, String newColName){
        String sb = "ALTER TABLE " + tableName +
                " ADD COLUMN " + newColName + " INT REFERENCES " + foreignTable +
                "(id)";
        HelperOrm.executeStatement(conn, sb);
    }

    public static void create1To1Relationship(){

    }
    public static void create1ToManyRelationship(){

    }
    public static String createManyToManyRelationship(String leftTable, String rightTable){
        String[] tableNames = getTableNameArray(leftTable, rightTable);
        String junctionTableName = leftTable + "_" + rightTable;
        CustomORM.buildTable(junctionTableName, new HashMap<>());
        for(String tableName : tableNames){
            CustomORM.addForeignKey(junctionTableName, tableName, tableName + "_id");
        }
        return junctionTableName;
    }

    // Each HashMap should accept the tableName and id that user would like to link
    public static ResultSet linkRows(HashMap<String, Integer> leftTable, HashMap<String, Integer> rightTable){
        String[] tableNames = getTableNameArray(leftTable.keySet().toArray()[0].toString(), rightTable.keySet().toArray()[0].toString());
        String junctionTableName =
                tableNames[0]
                        + "_" +
                        tableNames[1];
        return linkRows(junctionTableName, leftTable, rightTable);
    }

    public static void foo(HashMap<String, Integer>[] hm){
        Arrays.stream(hm).sorted();
    }

    public static ResultSet linkRows(String junctionTableName,
                                     HashMap<String, Integer> leftTable,
                                     HashMap<String, Integer> rightTable){

        String[] tableNames = getTableNameArray(
                leftTable.keySet().toArray()[0].toString(),
                rightTable.keySet().toArray()[0].toString()
        );
        Object entry1 = leftTable.get(tableNames[0]);
        Object entry2 = rightTable.get(tableNames[1]);
        int id = CustomORM.addRow(junctionTableName, entry1, entry2);
        return HelperOrm.executeQuery(conn, "SELECT * FROM " + junctionTableName + " WHERE id=" + id);
    }


    @NotNull
    private static String[] getTableNameArray(String ...tableNames) {
        return Arrays.stream(tableNames).map((name)->
            HelperOrm.sanitizeName(name)
        ).toArray(String[]::new);
    }
}
