package ORM;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.HashMap;

import static ORM.CustomORM.connect;
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
        assertEquals("NUMERIC(15, 7)", dbl);
        assertEquals("BOOLEAN", bool);
        assertEquals("SMALLINT", byt);
        assertEquals("SMALLINT", shrt);
        assertEquals("INTEGER", integer);
        assertEquals("BIGINT", lng);
        assertNull(invalid);
    }

    @Test
    void buildValues() {
        String defaultStr = HelperOrm.buildValues(4, true);
        String noDefaultStr = HelperOrm.buildValues(2, false);

        assertEquals("(default, ?, ?, ?, ?)", defaultStr);
        assertEquals("(?, ?)", noDefaultStr);
    }


    @Test
    void buildColumn(){
        HashMap<String, Class> testMap = new HashMap<String, Class>();
        testMap.put("Name", String.class);
        testMap.put("Age", Integer.class);
        testMap.put("Alive", Boolean.class);
        testMap.put("Funds", Float.class);

        String testing = HelperOrm.buildColumn(testMap);

        assertEquals(",alive BOOLEAN, age INTEGER, name VARCHAR(75), funds NUMERIC(10, 2)", testing);
    }

    @Test
    void sanitizeTest(){
        String testString = "T E s T i N g";
        String after = HelperOrm.sanitizeName(testString);
        assertEquals("t_e_s_t_i_n_g", after);
    }


}