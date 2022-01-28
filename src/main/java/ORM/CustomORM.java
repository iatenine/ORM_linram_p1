package ORM;

import logging.ORMLogger;

import java.sql.*;

import static ORM.HelperOrm.buildColumn;

public class CustomORM{

    static Connection conn = null;

    public static boolean connect(){
        conn = JDBCConnection.getConnection();
        return conn != null;
    }

    public static boolean connect(String endpoint, String username, String password) {
            try{
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                Connection testConn  = DriverManager.getConnection(url, username, password);
                if(testConn instanceof Connection)
                    conn =testConn;
                return true;
            } catch (SQLException e){
                ORMLogger.logger.error(e.getSQLState());
                for (StackTraceElement elem : e.getStackTrace()) {
                    ORMLogger.logger.info(elem);
                }
                return false;
            }
    }

    public static String buildTable(String tableName, String[] colNames, Object[] dataTypes) {

        if (colNames.length != dataTypes.length) {
            return null;
        }
        String str = buildColumn(colNames, (Class[]) dataTypes);


        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "( id serial primary key "
                + str
                + ")";
        String sql2 = "Select * From " + tableName + ";";
        try {
            ResultSet rs;
            PreparedStatement st = conn.prepareStatement(sql2);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            st.executeQuery();
            return tableName;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            System.out.println(sql);
        }
    }

        public static void dropTable(String tableName){
        String sql = "Drop Table " + tableName + ";";
        try{
            PreparedStatement st = conn.prepareStatement(sql);
            st.execute();

        } catch (SQLException e){
            e.printStackTrace();
        }

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

            String sql = "SELECT * FROM " + tableName + " WHERE id = " + id + ";";

            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int count = 0;
            while(rs.next()){
                count++;
            }
            if(count == 1){
                String query = "DELETE FROM " + tableName + " WHERE id = " + id + ";";
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

    public static void main(String[] args) {
        System.out.println();
    }
}
