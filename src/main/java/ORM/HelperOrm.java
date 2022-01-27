package ORM;

import java.util.Arrays;

public class HelperOrm {

    String[] colNames = {
            "name",
            "favorite_letter",
            "net_worth",          // float assumes 2 decimals
            "memorization_of_pi", // double should be more precise
            "alive",
            "age",
            "high_score",
            "computer memory",
            "date_of_birth"
    };


    public static String convertDataType(Class clazz){
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
    public static void buildTypes(){

    }
    public static String buildColumn(String [] columns, Class[] type){
        StringBuilder columnNames = new StringBuilder(",");
        for(int i = 0; i < columns.length;i++){

            if(i == columns.length-1){
                columnNames.append(columns[i]+ " " + convertDataType(type[i]) );
            } else {
                columnNames.append(" "+ columns[i]+ " " + convertDataType(type[i]) + " , ");
            }
        }
        String str = columnNames.toString();

        System.out.println(str);

        return str;
    }

    public static void main(String[] args) {
        String[] colNames = {
                "name",
                "favorite_letter",
                "net_worth",          // float assumes 2 decimals
                "memorization_of_pi", // double should be more precise
                "alive",
                "age",
                "high_score",
                "computer memory",
                "date_of_birth"
        };
        Class[] dataTypes = {
                String.class,
                Character.class,
                Float.class,
                Double.class,
                Boolean.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class
        };

        buildColumn(colNames, dataTypes);


        //System.out.println(convertDataType(a));
    }

}
