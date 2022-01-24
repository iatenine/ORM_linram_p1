package ORM;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.Arrays;

public class CustomORM{

    static Connection conn = null;

    public static boolean connect(String endpoint, String username, String password) {


        if(conn == null){

            try{
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                conn  = DriverManager.getConnection(url, username, password);
                return true;
            } catch (SQLException e){
                e.printStackTrace();
            }

        }

        return true;
    }

    public static String buildTable(String tableName, String[] colNames, Object[] dataTypes){

        if(colNames.length != dataTypes.length){
            return null;
        }
        try {
            String sql = "CREATE TABLE IF NOT EXISTS" + tableName + "( id serial not NULL,\n"
                    + Arrays.toString(colNames) + " " + Arrays.toString(dataTypes) + "\n"
                    + "PRIMARY KEY(id))";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM " + tableName + "WHERE id == '" + id + "';";
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int count = 0;
            while(rs.next()){
                count++;
            }
            if(count == 1){
                String query = "DELETE FROM " + tableName + "WHERE id == '" + id + "';";
                stmt.execute(query);
            } else {
                return null;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rs;
    }
}
