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

}
