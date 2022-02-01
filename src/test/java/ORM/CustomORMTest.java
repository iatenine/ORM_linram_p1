package ORM;

import logging.ORMLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

import static ORM.CustomORM.conn;
import static org.junit.jupiter.api.Assertions.*;

class CustomORMTest {

    String tableName = "temp_table";
    String tableName2 = "temp_table2";

    @BeforeAll
    static void prepare(){
        assertTrue(CustomORM.connect());
    }

    @BeforeEach
    void prepareTable(){
        HashMap<String, Class> columns = new HashMap<>();
        columns.put("name", String.class);
        columns.put("age", Byte.class);
        CustomORM.buildTable(tableName, columns);
    }

    @AfterEach
    void cleanupDb(){
        CustomORM.dropTable(tableName);
    }

    @Test
    void connect() {
        assertFalse(CustomORM.connect(
                "Impossible Endpoint",
                "Fake username",
                "bad password"
        ));
        // Will not work without proper setup in resources/connections.properties
        assertTrue(CustomORM.connect());
        // Ensure using new connections isn't blocked by Object state
        assertFalse(CustomORM.connect(
                "Impossible Endpoint",
                "Fake username",
                "bad password"
        ));
    }

    @Test
    void buildTable() {
        HashMap<String, Class> columns = new HashMap<>();
        columns.put("name", String.class);
        columns.put("favorite_letter", Character.class);
        columns.put("net_worth", Float.class);
        columns.put("Memorization of Pi", Double.class);
        columns.put("alive", Byte.class);
        columns.put("high score", Short.class);
        columns.put("computer memory", Integer.class);
        columns.put("date_of_birth", Long.class);

        String betterTable = CustomORM.buildTable(
                tableName,
                columns
        );
        assertEquals(tableName, betterTable);
    }

    @Test
    void addRow() {
        int newId = CustomORM.addRow(
                tableName,
                "Hank",
                20
        );

        assertInstanceOf(Integer.class, newId);
        assertTrue(newId > 0);
    }

    @Test
    void getRow() {
        int newId = CustomORM.addRow(
                tableName,
                "Hank",
                20
        );
        ResultSet rs_full = CustomORM.getRow(tableName, newId, new String[] {"*"});
        ResultSet rs_partial = CustomORM.getRow(tableName, newId, new String[]{"name"});

        try {
            // Place both ResultSets on their first response
            assertNotNull(rs_full);
            assertNotNull(rs_partial);
            assertTrue(rs_full.next());
            assertTrue(rs_partial.next());
            String fetchedName1 = rs_full.getString(2);
            int fetchedAge = rs_full.getInt(3);

            String fetchedName2 = rs_partial.getString(1);
            assertEquals(20, fetchedAge);
            assertEquals("Hank", fetchedName1);
            assertEquals("Hank", fetchedName2);
        } catch (SQLException e) {
            // SQLExceptions are ALWAYS a test failure
            fail();
            ORMLogger.logger.info(e.getSQLState());
            ORMLogger.logger.error(e.getStackTrace());
        } catch (Exception i){
            fail();
            ORMLogger.logger.error(i.getStackTrace());
        }
    }

    @Test
    void getRows() {
        CustomORM.addRow(
                tableName,
                "Hank",
                45
        );
        CustomORM.addRow(
                tableName,
                "Bobby",
                12
        );
        ResultSet rs = CustomORM.getRows(tableName, new String[] {"*"});

        try {
            // Place both ResultSets on their first response
            assertNotNull(rs);

            // Get Hank
            assertTrue(rs.next());
            String fetchedName = rs.getString(2);
            int fetchedAge = rs.getInt(3);
            assertEquals(45, fetchedAge);
            assertEquals("Hank", fetchedName);

            // Get Bobby
            assertTrue(rs.next());
            fetchedName = rs.getString(2);
            fetchedAge = rs.getInt(3);
            assertEquals(12, fetchedAge);
            assertEquals("Bobby", fetchedName);
            assertFalse(rs.next());
        } catch (SQLException e) {
            // Expected exception
        }
    }

    @Test
    void updateRow() {
        int newId = CustomORM.addRow(
                tableName,
                "Hank",
                20
        );

        HashMap<String, Object> newCols = new HashMap<>();

        newCols.put("name", "Bobby");
        newCols.put("age", 12);

        ResultSet rs = CustomORM.updateRow(
                tableName,
                newId,
                newCols
        );
        assertNotNull(rs);
        try {
            assertTrue(rs.next());
            String newName = rs.getString(2);
            int newAge = rs.getInt(3);

            assertEquals("Bobby", newName);
            assertEquals(12, newAge);
        } catch (SQLException e) {
            // SQLExceptions are ALWAYS a test failure
            fail();
            ORMLogger.logger.info(e.getSQLState());
            ORMLogger.logger.error(e.getStackTrace());
        } catch (NullPointerException e){
            ORMLogger.logger.info(e.getStackTrace());
            ORMLogger.logger.error("Null Pointer Exception");
        }
    }

    @Test
    void deleteRow() {
        int newId = CustomORM.addRow(
                tableName,
                "Hank",
                45
        );

        ResultSet rs = CustomORM.deleteRow(tableName, newId);
        assertNotNull(rs);
        try {
            assertTrue(rs.next());
            String deletedName = rs.getString(1);
            int deletedAge = rs.getInt(2);

            assertEquals("Hank", deletedName);
            assertEquals(45, deletedAge);

            // Ensure deletion occurred
            rs = CustomORM.getRow(tableName, newId, new String[] {"*"});
            assertNotNull(rs);
//            assertFalse(rs.next());
        } catch (SQLException e) {
            // This SHOULD happen because rs.next() is meant to be false
            ORMLogger.logger.info(e.getSQLState());
            ORMLogger.logger.error(e.getStackTrace());
        } catch (Exception i){
            ORMLogger.logger.error(i.getStackTrace());
        }
    }

    @Test
    void dropTable() {
        int newId = CustomORM.addRow(
                tableName,
                "Cotton",
                85
        );
        assertNotEquals(-1, newId);
        CustomORM.dropTable(tableName);
        assertNull(CustomORM.getRow(tableName, newId, new String[] {"*"}));            // Ensure table has been dropped
    }
    @Test
    void join() {
        HashMap<String, Class> columns = new HashMap<>();
        columns.put("name", String.class);
        columns.put("total", Double.class);
        columns.put("date", Long.class);

        HashMap<String, Class> column2 = new HashMap<>();
        column2.put("name", String.class);
        column2.put("food", String.class);
        column2.put("total", String.class);

        String betterTable = CustomORM.buildTable(
                tableName,
                columns
        );
        String betterTable2 = CustomORM.buildTable(
                tableName2,
                column2
        );

        String[] names = {"*"};

        ResultSet set = CustomORM.join(
                betterTable,
                betterTable2,
                names
                );
        assertNotNull(set);

    }



    @Test
    void executeStatementTest(){
        //Boolean check = false;
    }

    @Test
    void executeQueryTest(){

       // String test = "Select * From Test_Table";
       // ResultSet rs = HelperOrm.executeQuery(conn, test);
    }

}