package app;

import Research.TestInheritance;

import java.lang.reflect.Field;

public class Main {
    public static void main(String[] args) {

        Field[] field = TestInheritance.class.getDeclaredFields();

        System.out.println(field[0].getName()); // Outputs b
        System.out.println(field[0].getType().toString().equals("byte")); // true
        System.out.println(field[0].getType().isPrimitive()); //true
        System.out.println(field[0].getType().isArray()); //false

        System.out.println(field[1].getName()); // s
        System.out.println(field[1].getType().toString()); // class [S
        System.out.println(field[1].getType().isArray()); // true
        System.out.println(field[1].getType().arrayType().toString().equals("short"));  //true
    }
}
