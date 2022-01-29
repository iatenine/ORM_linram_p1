package ORM;

import logging.ORMLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;

public class HelperOrm {

    @Contract(pure = true)
    static @NotNull String buildValues(int args, boolean provideDefault){
        StringBuilder sb = new StringBuilder("(");
        if(provideDefault)
            sb.append("default, ");
        for(int i = 0; i < args; i++){
            sb.append("?");
            if(i != args -1)
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }


    static @Nullable String convertDataType(@NotNull Class clazz){

        String[] words = clazz.getName().split("\\.");
        String className = words[words.length-1];
        switch (className){
            case "String" -> {
                return "VARCHAR(75)";
            }
            case "Character"->{
                return "VARCHAR(1)";
            }
            case "Float" -> {
                return "NUMERIC(10, 2)";
            }
            case "Double" -> {
                return "NUMERIC(15, 7)";
            }
            case "Boolean" -> {
                return "BOOLEAN";
            }
            case "Byte", "Short" -> {
                return "SMALLINT";
            }
            case "Integer" -> {
                return "INTEGER";
            }
            case "Long" -> {
                return "BIGINT";
            }
            default -> {
                return null;
            }
        }
    }

    static String buildValues(Object ...entries){

        StringBuilder values = new StringBuilder("( default, ");
        for(Object valName : entries){
            Class clazz = valName.getClass();
            String[] words = clazz.getName().split("\\.");
            String className = words[words.length-1];
            if("String".equals(className) || "Character".equals(className)){
                values.append("'" + valName + "', "  );
            } else {
                values.append(valName + ", "  );
            }



        }

        String temp = values.toString().replaceAll(", $", "");

        return temp + ") RETURNING id";


    }


    static String buildColumn(HashMap<String, Class> columns){
        StringBuilder columnNames = new StringBuilder(",");
        for(String colName : columns.keySet()){
            String sanitizedColName = sanitizeName(colName);
            columnNames.append(sanitizedColName + " " + convertDataType(columns.get(colName)) + ", ");
        }
        return columnNames.toString().replaceAll(", $", "");
    }

    static String sanitizeName(String s){
        return s.replaceAll(" ", "_").toLowerCase(Locale.ROOT);
    }

    static void executeStatement(Connection conn, String sql){
        try{
            PreparedStatement st = conn.prepareStatement(sql);
            st.execute();
        } catch (SQLException e){
            ORMLogger.logger.info(e.getSQLState());
            for(StackTraceElement msg : e.getStackTrace()){
                ORMLogger.logger.error(msg);
            }
        }
    }

    // Uncomment when needed, currently drops test coverage below 80%
    static ResultSet executeQuery(Connection conn, String sql){
        ResultSet rs = null;
        try{
            PreparedStatement st = conn.prepareStatement(sql);
            rs = st.executeQuery();
        } catch (SQLException e){
            ORMLogger.logger.info(e.getSQLState());
            for(StackTraceElement msg : e.getStackTrace()){
                ORMLogger.logger.error(msg);
            }
        }

        return rs;
    }
}
