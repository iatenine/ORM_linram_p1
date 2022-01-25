package ORM;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;

import static org.junit.jupiter.api.Assertions.*;

class HelperOrmTest {

    @Test
    void convertDataType() {
        String str = HelperOrm.convertDataType(String.class);
        String chr = HelperOrm.convertDataType(Character.class);
        String flt = HelperOrm.convertDataType(Float.class);
        String dbl = HelperOrm.convertDataType(Double.class);
        String bool = HelperOrm.convertDataType(Boolean.class);
        String byt = HelperOrm.convertDataType(Byte.class);
        String shrt = HelperOrm.convertDataType(Short.class);
        String integer = HelperOrm.convertDataType(Integer.class);
        String lng = HelperOrm.convertDataType(Long.class);
        String invalid = HelperOrm.convertDataType(Array.class);

        assertEquals("VARCHAR(75)", str);
        assertEquals("VARCHAR(1)", chr);
        assertEquals("NUMERIC(10, 2)", flt);
        assertEquals("NUMERIC(15, 5)", dbl);
        assertEquals("BOOLEAN", bool);
        assertEquals("SMALLINT", byt);
        assertEquals("SMALLINT", shrt);
        assertEquals("INTEGER", integer);
        assertEquals("BIGINT", lng);
        assertNull(invalid);
    }
}