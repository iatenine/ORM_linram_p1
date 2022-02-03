package ORM;

import logging.ORMLogger;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class FKTests {
    int[] car_ids = new int[4];
    int[] driver_ids = new int[4];
    int[] owner_ids = new int[4];
    @BeforeAll
    static void connectDB(){
        assertTrue(CustomORM.connect());

    }

    // Each method needs a clean set of tables to work properly
    @BeforeEach
    void setUpTables() {
        //Need tables with appropriate 1:n, 1:1 and n:m relationships
        // cars, drivers, owners
        // owners:cars 1:many
        // drivers:cars 1:1
        // cars:owners many:many (joint ownership possible)
        HashMap<String, Class> driverMap = new HashMap<>();
        HashMap<String, Class> carMap = new HashMap<>();
        HashMap<String, Class> ownerMap = new HashMap<>();

        driverMap.put("name", String.class);
        ownerMap.put("name", String.class);
        carMap.put("model", String.class);

        CustomORM.createTable("owners", ownerMap);
        CustomORM.createTable("drivers", driverMap);
        CustomORM.createTable("cars", carMap);

        car_ids[0] = CustomORM.addRow("cars", "F-150");
        car_ids[1] = CustomORM.addRow("cars", "Silverado");
        car_ids[2] = CustomORM.addRow("cars", "Fiat 500");
        car_ids[3] = CustomORM.addRow("cars", "Camry");

        owner_ids[0] = CustomORM.addRow("drivers", "Hank");
        owner_ids[1] = CustomORM.addRow("drivers", "Bobby");
        owner_ids[2] = CustomORM.addRow("drivers", "Peggy");
        owner_ids[3] = CustomORM.addRow("drivers", "Dale");

        driver_ids[0] = CustomORM.addRow("owners", "Hank Hill");
        driver_ids[1] = CustomORM.addRow("owners", "Capital One");
        driver_ids[2] = CustomORM.addRow("owners", "Cotton Hill");
        driver_ids[3] = CustomORM.addRow("owners", "Wyatt's Towing");
    }

    @Test
    void addForeignKey(){
        CustomORM.addForeignKey("drivers", "cars", "car_id");
        ResultSet rs = HelperOrm.executeQuery(CustomORM.conn, """
            SELECT model FROM drivers d
            JOIN cars c on c.id=d.car_id
            """);
        assertNotNull(rs);
        // Test for false positives (SHOULD log an error)
        rs = HelperOrm.executeQuery(CustomORM.conn, """
            SELECT notAColumn FROM drivers d
            JOIN cars c on c.id=d.car_id
            """);
        assertNull(rs);
        ORMLogger.logger.info("Stack trace printout expected side effect of test suite execution");
    }

    @Test
    void getJoinedTables(){
        String[] names = {"name"};

        CustomORM.addForeignKey("owners", "drivers");

        ResultSet set = CustomORM.join(
                "owners",
                "drivers",
                "drivers_id",
                names,
                names);
        assertNotNull(set);
        try {
            assertFalse(set.next());
//            String ownerName = set.getString(1);
//            String driverName = set.getString(2);
//
//            assertEquals("Hank", driverName);
//            assertEquals("Hank Hill", ownerName);
        } catch (SQLException e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    void create1to1Relationship(){
        String[] colNames = CustomORM.create1To1Relationship("cars", "drivers");

        HashMap<String, Integer> row1 = new HashMap<>();
        HashMap<String, Integer> row2 = new HashMap<>();

        row1.put("cars", car_ids[0]);
        row2.put("drivers", driver_ids[0]);

        ResultSet rs = CustomORM.linkRows1To1(row1, row2);
        assertNotNull(rs);

        try {
            assertTrue(rs.next());
            assertEquals(car_ids[0], rs.getInt("cars_id"));
            assertEquals(driver_ids[0], rs.getInt("drivers_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void create1toManyRelationship(){
        fail();
    }

    @Test
    void manyToManyandLinkRows(){
        String jTableName = CustomORM.createManyToManyRelationship("cars", "owners");
        assertEquals("cars_owners", jTableName);
        // Try to get a join
        HashMap<String, Integer> left_row = new HashMap<>();
        HashMap<String, Integer> right_row= new HashMap<>();
        HashMap<String, Integer> otherOwner = new HashMap<>();

        left_row.put("cars", car_ids[0]);
        right_row.put("owners", owner_ids[0]);
        otherOwner.put("owners", owner_ids[1]);

        // Use linkRows method to simplify linking many-to-many relationship tables
        ResultSet rs = CustomORM.linkRowsManyToMany(left_row, right_row);
        ResultSet rs2 = CustomORM.linkRowsManyToMany(left_row, otherOwner);

        assertNotNull(rs);
        assertNotNull(rs2);
        try {
            assertTrue(rs.next());
            assertTrue(rs2.next());

            int car_id = rs.getInt("cars_id");
            int cars_id2 = rs2.getInt("cars_id");

            int owner1_id = rs.getInt("owners_id");
            int owner2_id = rs2.getInt("owners_id");

            assertNotEquals(owner1_id, owner2_id);
            assertEquals(car_id, cars_id2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @AfterEach
    void cleanUpTables(){
        CustomORM.dropTable("cars_owners");
        CustomORM.dropTable("owners");
        CustomORM.dropTable("drivers");
        CustomORM.dropTable("cars");
    }
}
