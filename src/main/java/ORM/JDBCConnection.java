package ORM;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCConnection {

    private static Connection conn = null;

    public static Connection getConnection(){
        // Establish conn if one doesn't exist, otherwise return current
        if(conn != null)
            return conn;

        Properties props = new Properties();
        try {
            props.load(JDBCConnection.class.getClassLoader().getResourceAsStream("connection.properties"));
            conn = DriverManager.getConnection(props.getProperty("endpoint"), props.getProperty("username"), props.getProperty("password"));
        }
        catch (SQLException | IOException e){
            e.printStackTrace();
        }

        return conn;
    }
}
