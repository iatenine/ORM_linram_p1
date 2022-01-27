package ORM;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelperOrm {

    @Contract(pure = true)
    public static @NotNull String buildValues(int args, boolean provideDefault){
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


    public static @Nullable String convertDataType(@NotNull Class clazz){

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
