package ORM;

import logging.ORMLogger;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class FKTests {
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

        CustomORM.buildTable("owners", ownerMap);
        CustomORM.buildTable("drivers", driverMap);
        CustomORM.buildTable("cars", carMap);

        CustomORM.addRow("cars", "F-150");
        CustomORM.addRow("cars", "Silverado");
        CustomORM.addRow("cars", "Fiat 500");
        CustomORM.addRow("cars", "Camry");

        CustomORM.addRow("drivers", "Hank");
        CustomORM.addRow("drivers", "Bobby");
        CustomORM.addRow("drivers", "Peggy");
        CustomORM.addRow("drivers", "Dale");

        CustomORM.addRow("owners", "Hank Hill");
        CustomORM.addRow("owners", "Capital One");
        CustomORM.addRow("owners", "Cotton Hill");
        CustomORM.addRow("owners", "Wyatt's Towing");
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
        fail();
    }

    @Test
    void create1to1Relationship(){
        fail();
    }

    @Test
    void create1toManyRelationship(){
        fail();
    }

    @Test
    void createManyToManyRelationship(){
        fail();
    }


    @AfterEach
    void cleanUpTables(){
        CustomORM.dropTable("owners");
        CustomORM.dropTable("drivers");
        CustomORM.dropTable("cars");
    }
}
