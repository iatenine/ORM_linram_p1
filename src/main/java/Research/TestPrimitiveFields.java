package Research;

import java.lang.reflect.*;

public class TestPrimitiveFields {
    // Numerics
    //byte, short, int, long
    //double, float
    private byte b;
    short s[];
    int i = 200;
    long l = 40;

    double d = 40.3;
    float f = 60.9f;

    // Non-numeric
    char c = 'd';
    boolean on = true;

    //Uninitialized
    int uninitialized;

    public static void main(String[] args) {
        Field[] field = TestPrimitiveFields.class.getDeclaredFields();

        //given fields of
        // private byte b;
        // private short[] s;

        System.out.println(field[0].getName()); // Outputs b
        System.out.println(field[0].getType().toString().equals("byte")); // true
        System.out.println(field[0].getType().isPrimitive()); //true
        System.out.println(field[0].getType().isArray()); //false

        System.out.println(field[1].getName()); // s
        System.out.println(field[1].getType()); // class [S
        System.out.println(field[1].getType().isArray()); // true
        System.out.println(field[1].getType().arrayType().toString().equals("short"));  //true
    }

    static public void foo(){
        bar();
    }

    private static void bar(){

    }
}
