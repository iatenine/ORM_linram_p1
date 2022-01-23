package ORM;

import java.sql.Connection;
import java.sql.ResultSet;

public class CustomORM{

    Connection conn = null;

    public static boolean connect(String endpoint, String username, String password) {
        return false;
    }

    public static String buildTable(String tableName, String[] colNames, Object[] dataTypes){
        return null;
    }

    public static int addRow(String tableName, Object ...entries){
        return -1;
    }

    public static ResultSet getRow(String tableName, int id, String[] colNames){
        return null;
    }

    // update row
    public static ResultSet updateRow(String tableName, int id, String[] colNames, String[] values){
        return null;
    }

    // delete row
    public static ResultSet deleteRow(String tableName, int id){
        return null;
    }
}
