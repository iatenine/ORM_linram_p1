package ORM;

import logging.ORMLogger;

import java.sql.*;
import java.util.HashMap;
import java.util.Locale;

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
        }
        return -1;
    }

    public static ResultSet getJoinedTables(String firstTable, String foreignTable, String[] colNames, String condition){
        return null;
    }

    public static ResultSet getRow(String tableName, int id, String[] colNames){
        StringBuilder sb = new StringBuilder("SELECT ");
        for(String colName : colNames){
            sb.append(HelperOrm.sanitizeName(colName));
            sb.append(" ");
        }
        sb.append("FROM ");
        sb.append(tableName);
        sb.append(" WHERE id=").append(id);
        return executeQuery(conn, sb.toString());
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

    // Create Foreign Keys
    public static void addForeignKey(String tableName, String foreignTable, String newColName){
        StringBuilder sb = new StringBuilder("ALTER TABLE ").append(tableName);
        sb.append(" ADD COLUMN ").append(newColName).append(" INT REFERENCES ").append(foreignTable);
        sb.append("(id)");
        HelperOrm.executeStatement(conn, sb.toString());
    }

    public static void create1To1Relationship(){

    }
    public static void create1ToManyRelationship(){

    }
    public static void createManyToManyRelationship(){

    }
}
