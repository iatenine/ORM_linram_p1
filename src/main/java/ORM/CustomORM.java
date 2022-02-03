package ORM;

import logging.ORMLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import static ORM.HelperOrm.*;

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

    public static String createTable(String tableName, HashMap<String, Class> columns) {
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
        String sql = "Drop Table if exists " + tableName + " CASCADE;";
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

    public static ResultSet join(String firstTable, String secondTable){
        String [] EVERYTHING = {"*"};
        return join(firstTable, secondTable, secondTable+"_id", EVERYTHING, EVERYTHING);
    }

    public static ResultSet join(String firstTable, String secondTable, String fkColName, String[] colNames1, String[] colNames2){
        String alias1 = "t1";
        String alias2 = "t2";
        String sql = "Select " + aliases(alias1, alias2, colNames1, colNames2) +
                "From " +
                firstTable +
                " " +
                alias2 +
                " Join " +
                secondTable +
                " " +
                alias1 +
                " On " +
                alias2 +
                "." + fkColName +
                " = " +
                alias1 + ".id";


        return executeQuery(conn, sql);
    }

    // Create Foreign Keys
    public static void addForeignKey(String tableName, String foreignTable){
        addForeignKey(tableName, foreignTable, foreignTable+"_id");
    }

    public static void addForeignKey(String tableName, String foreignTable, String newColName){
        String sql = "ALTER TABLE " + tableName + " "+
                "ADD COLUMN " + newColName + " INT, "+
                "ADD FOREIGN KEY (" + newColName +") "+
                "REFERENCES " + foreignTable + "(id) "+
                "ON DELETE CASCADE";
        HelperOrm.executeStatement(conn, sql);
    }

    public static String[] create1To1Relationship(String tableName1, String tableName2){
        String[] tableNames = getTableNameArray(tableName1, tableName2);
        String fkColName1 = tableNames[1]+"_id";
        String fkColName2 = tableNames[0]+"_id";
        CustomORM.addForeignKey(tableName1, tableName2, fkColName1);
        CustomORM.addForeignKey(tableName2, tableName1, fkColName2);
        return new String[]{fkColName1, fkColName2};
    }
    public static void create1ToManyRelationship(){


    }
    public static String createManyToManyRelationship(String leftTable, String rightTable){
        String[] tableNames = getTableNameArray(leftTable, rightTable);
        String junctionTableName = leftTable + "_" + rightTable;
        CustomORM.createTable(junctionTableName, new HashMap<>());
        for(String tableName : tableNames){
            CustomORM.addForeignKey(junctionTableName, tableName);
        }
        return junctionTableName;
    }

    //linkRows1to1 tableName1, tableName2, fkColumnName, fkColumnName2
    public static ResultSet linkRows1To1(HashMap<String, Integer> row1,
                                         HashMap<String, Integer> row2){
        String tableName1 = HelperOrm.sanitizeName(row1.keySet().toArray()[0].toString());
        String tableName2 = HelperOrm.sanitizeName(row2.keySet().toArray()[0].toString());
        int id1 = row1.get(tableName1);
        int id2 = row2.get(tableName2);

        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> hm2 = new HashMap<>();
        hm.put(tableName2+"_id", id2);
        hm2.put(tableName1+"_id", id1);

        CustomORM.updateRow(tableName1, id1, hm);
        CustomORM.updateRow(tableName2, id2, hm2);


        return join(tableName1, tableName2);
    }


    //linkRows1toMany tableName1, tableName2, fkColumnName

    // Each HashMap should accept the tableName and id that user would like to link
    public static ResultSet linkRowsManyToMany(HashMap<String, Integer> leftTable, HashMap<String, Integer> rightTable){
        String[] tableNames = getTableNameArray(leftTable.keySet().toArray()[0].toString(), rightTable.keySet().toArray()[0].toString());
        String junctionTableName =
                tableNames[0]
                        + "_" +
                        tableNames[1];
        return linkRowsManyToMany(junctionTableName, leftTable, rightTable);
    }

    public static ResultSet linkRowsManyToMany(String junctionTableName,
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
        return Arrays.stream(tableNames).map(HelperOrm::sanitizeName
        ).toArray(String[]::new);
    }


}
