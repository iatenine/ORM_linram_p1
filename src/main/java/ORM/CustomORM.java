package ORM;

import logging.ORMLogger;

import java.sql.*;
import java.util.Arrays;

public class CustomORM{

    static Connection conn = null;

    public static boolean connect(String endpoint, String username, String password) {


        if(conn == null){

            try{
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                ORMLogger.logger.info(url);
                conn  = DriverManager.getConnection(url, username, password);

                return true;
            } catch (SQLException e){

                e.printStackTrace();
                return false;
            }

        }

        return true;
    }

    public static String buildTable(String tableName, String[] colNames, Object[] dataTypes){

        if(colNames.length != dataTypes.length){
            return null;
        }
        try {
            String sql = "CREATE TABLE IF NOT EXISTS" + tableName + "( id serial not NULL,"
                    + Arrays.toString(colNames) + " " + Arrays.toString(dataTypes) + " "
                    + "PRIMARY KEY(id))";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void dropTable(String tableName){

    };

    public static int addRow(String tableName, Object ...entries){
        return -1;
    }

    public static ResultSet getRow(String tableName, int id, String[] colNames){
        return null;
    }

    // update row
    public static ResultSet updateRow(String tableName, int id, String[] colNames, Object[] values){
        return null;
    }

    // delete row
    public static ResultSet deleteRow(String tableName, int id){

        ResultSet rs = null;
        try {
            //connect()
            //conn.connect();
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


    private static String buildTableString(String tableName, String[] colNames, Object[] dataTypes){


        String sql = "CREATE TABLE IF NOT EXISTS" + tableName + "( id serial not NULL,";

            sql += ");";

        return null;
    }
}
