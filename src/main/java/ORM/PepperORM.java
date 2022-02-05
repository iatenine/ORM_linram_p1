package ORM;

import logging.ORMLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import static ORM.HelperOrm.*;

public class PepperORM {

    static Connection conn = null;

    /** boolean connection method; does not accept a value of null
     *
     * @return  returns boolean of not null
     */
    public static boolean connect(){
        conn = JDBCConnection.getConnection();
        return conn != null;
    }
    /** Establishes the connection to database excepts AWS credentials as arguments
     *
     * @param endpoint  String of database endpoint
     * @param username  String of username
     * @param password  String of password
     * @return          returns boolean value of true if connection is established,
     *                  false if an exception is caught
     */
    public static boolean connect(String endpoint, String username, String password) {
            try{
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                Connection testConn = DriverManager.getConnection(url, username, password);
                if(testConn != null)
                    conn = testConn;
                return (conn instanceof JDBCConnection);
            } catch (SQLException e){
                ORMLogger.logger.error(e.getSQLState());
                for (StackTraceElement elem : e.getStackTrace()) {
                    ORMLogger.logger.info(elem);
                }
                return false;
            }
    }















    /** Directly calls to create a new Table creation int the database, hashmap contains specified column names
     *  along with the data type for each
     *
     * @param tableName name of new table
     * @param columns specified projected columns
     * @return  name of the newly created table
     */
    public static String createTable(String tableName, HashMap<String, Class> columns) {
        // Protect against errors caused by spaces and eliminate all UPPERCASE..age
        tableName = tableName.replaceAll(" ", "_").toLowerCase(Locale.ROOT);
        String str = buildColumn(columns);
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "( id serial primary key "
                + str
                + ")";
        HelperOrm.executeStatement(conn, sql);
        return tableName;
    }
    /** Directly Drops the named table within the database, method requires specification of the table name
     *
     * @param tableName Name of table to drop
     */
    public static void dropTable(String tableName){
        String sql = "Drop Table if exists " + tableName + " CASCADE;";
        HelperOrm.executeStatement(conn, sql);
    }













    /** Adds A new row into a specified table with values for established columns
     *
     * @param tableName Specified table to make the insert
     * @param entries   takes in a dynamic size of values to insert in the newly added row
     * @return          returns value of the first column in the new added row
     */
    public static int addRow(String tableName, Object ...entries){
        ResultSet rs;
        String sql = "Insert Into " + tableName + " Values " + buildValues(entries);
        rs = HelperOrm.executeQuery(conn, sql);
        try {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    /** Project the ResultSet of a single row within a table
     *
     * @param tableName the ideal table name in which the row exist
     * @param id        unique key in which to specify row
     * @param colNames  intended columns to project
     * @return          returns result set of the sql query
     */
    public static ResultSet getRow(String tableName, int id, String[] colNames){
        String sql = HelperOrm.selectStatementBuilder(tableName, colNames) + " WHERE id=" + id;
        return executeQuery(conn, sql);
    }
    /** Project the resultSet of multiple rows within a table
     *
     * @param tableName Name of Table
     * @param colNames specified projected columns
     * @return         returns the result set of query
     */
    public static ResultSet getRows(String tableName, String[] colNames){
        String sql = HelperOrm.selectStatementBuilder(tableName, colNames);
        return executeQuery(conn, sql);
    }
    /** Update the value(s) in a single row in a table
     *
     * @param tableName table name in which row is exist
     * @param id        unique identifying key
     * @param newEntries selected entries containing HashMap<String(colName), Object(newValue)>
     * @return          returns ResultSet with all columns existing in table
     */
    public static ResultSet updateRow(String tableName, int id, HashMap<String, Object> newEntries){
        //UPDATE tableName SET colName=newValue WHERE id=target RETURNING *;
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(tableName);
        sb.append(" SET ");
        for(String colName : newEntries.keySet()){
            sb.append(HelperOrm.sanitizeName(colName));
            sb.append("=");
            sb.append(HelperOrm.formatValues(newEntries.get(colName)));
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(" WHERE id=");
        sb.append(id);
        sb.append(" RETURNING *");
        return HelperOrm.executeQuery(conn, sb.toString());
    }
    /** Delete a single specified row in the database
     *
     * @param tableName Name of table which row exist
     * @param id    unique identifier to specify row
     * @return      returns an asserted empty result set
     */
    public static ResultSet deleteRow(String tableName, int id){
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM " + tableName + " WHERE id = " + id + ";";
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int count = 0;
            while(rs.next()){
                count++;
            }
            if(count == 1){
                String query = "DELETE FROM " + tableName + " WHERE id = " + id + " RETURNING *;";
                return HelperOrm.executeQuery(conn, query);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }















    /** table join operations projects all columns within the join
     *
     * @param firstTable    name of left table in the join
     * @param secondTable   name of right table in the join
     * @return              returns ResultSet projecting all columns within the join
     */
    public static ResultSet join(String firstTable, String secondTable){
        String [] EVERYTHING = {"*"};
        return join(firstTable, secondTable, secondTable+"_id", EVERYTHING, EVERYTHING);
    }
    /** Projects columns specified by user; The sql string uses aliases
     *
     * @param firstTable   name of left table
     *
     * @param secondTable  name of right table
     * @param fkColName    column name of foreign table identifier
     * @param colNames1    specified columns to project for left table
     * @param colNames2    specified columns to project for right table
     * @return             returns ResultSet consisting of specified columns from both left and right tables
     */
    public static ResultSet join(String firstTable, String secondTable, String fkColName, String[] colNames1, String[] colNames2){
        String alias1 = "t1";
        String alias2 = "t2";
        String sql = "Select " + aliases(alias1, alias2, colNames1, colNames2) +
                "From " +
                firstTable +
                " " +
                alias2 +
                " Join " +
                secondTable +
                " " +
                alias1 +
                " On " +
                alias2 +
                "." + fkColName +
                " = " +
                alias1 + ".id";
        return executeQuery(conn, sql);
    }












    /** Takes in Table and the foreign table and specifies a foreign key column
     *
     * @param tableName specified table Name
     * @param foreignTable  specified foreign table
     */
    // Create Foreign Keys
    public static void addForeignKey(String tableName, String foreignTable){
        addForeignKey(tableName, foreignTable, foreignTable+"_id");
    }
    /** Alters non-foreign table by adding a new column which is used to reference a foreign table
     *
     * @param tableName    Name of referencing table
     * @param foreignTable Name of referenced table
     * @param newColName   new column Name used to for reference
     */
    public static void addForeignKey(String tableName, String foreignTable, String newColName){
        String sql = "ALTER TABLE " + tableName + " "+
                "ADD COLUMN " + newColName + " INT, "+
                "ADD FOREIGN KEY (" + newColName +") "+
                "REFERENCES " + foreignTable + "(id) "+
                "ON DELETE CASCADE";
        HelperOrm.executeStatement(conn, sql);
    }










    /** adds foreign column on both left and right tables Returns String array containing FK column names
     *
     * @param tableName1 left table name
     * @param tableName2 right table name
     * @return           String array containing both new column names; referencing one another
     */
    public static String[] create1To1Relationship(String tableName1, String tableName2){
        String[] tableNames = getTableNameArray(tableName1, tableName2);
        String fkColName1 = tableNames[1]+"_id";
        String fkColName2 = tableNames[0]+"_id";
        PepperORM.addForeignKey(tableName1, tableName2, fkColName1);
        PepperORM.addForeignKey(tableName2, tableName1, fkColName2);
        return new String[]{fkColName1, fkColName2};
    }
    /** add new referencing column in right table(many)
     *
     * @param tableName1 Name of left table (one)
     * @param tableName2 Name of right table (many)
     * @return           Returns String of foreign column name
     */
    public static String create1ToManyRelationship(String tableName1, String tableName2) {

        String fkColName2 = HelperOrm.sanitizeName(tableName1) + "_id";
        PepperORM.addForeignKey(tableName2,tableName1,fkColName2);
        return fkColName2;
    }
    /** Forms a junction table between specified left and right tables
     *
     * @param leftTable  Name of left table
     * @param rightTable Name of right table
     * @return           return String Name of junction table
     */
    public static String createManyToManyRelationship(String leftTable, String rightTable){
        String[] tableNames = getTableNameArray(leftTable, rightTable);
        String junctionTableName = leftTable + "_" + rightTable;
        PepperORM.createTable(junctionTableName, new HashMap<>());
        for(String tableName : tableNames){
            PepperORM.addForeignKey(junctionTableName, tableName);
        }
        return junctionTableName;
    }











    /** links rows one to one for table1 and table 2
     *
     * @param row1 row from table first table
     * @param row2 row from second table
     * @return  returns result set of the joined table (one to one)
     */
    //linkRows1to1 tableName1, tableName2, fkColumnName, fkColumnName2
    public static ResultSet linkRows1To1(HashMap<String, Integer> row1,
                                         HashMap<String, Integer> row2){
        String tableName1 = HelperOrm.sanitizeName(row1.keySet().toArray()[0].toString());
        String tableName2 = HelperOrm.sanitizeName(row2.keySet().toArray()[0].toString());
        int id1 = row1.get(tableName1);
        int id2 = row2.get(tableName2);

        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> hm2 = new HashMap<>();
        hm.put(tableName2+"_id", id2);
        hm2.put(tableName1+"_id", id1);

        PepperORM.updateRow(tableName1, id1, hm);
        PepperORM.updateRow(tableName2, id2, hm2);


        return join(tableName1, tableName2);
    }
    /** Links rows one to many takes in hashMap for both one table and many table
     *  each hashmap has String: table Name and Integer: id
     *
     * @param one   first table HashMap containing table name and id
     * @param many  many table HashMap containing table name and id
     * @return  returns the ResultSet
     */
    //linkRows1toMany tableName1, tableName2, fkColumnName
    public static ResultSet linkRows1toMany(HashMap<String, Integer> one,HashMap<String,Integer> many){
        String oneTable = HelperOrm.sanitizeName(one.keySet().toArray()[0].toString());
        String manyTable = HelperOrm.sanitizeName(many.keySet().toArray()[0].toString());
        int id1 = many.get(manyTable);
        HashMap<String, Object> hm = new HashMap<>();
        hm.put(oneTable +"_id", id1);
        PepperORM.updateRow(manyTable, id1, hm);
        return join(manyTable, oneTable);
    }
    /** Link rows many to many; creates a separate junction table passes arguments: JunctionTable,
     *  leftTable HashMap, rightTable HashMap to overload linkRowsManyToMany
     *
     * @param leftTable  HashMap of leftTable containing String: table Name and Integer: id
     * @param rightTable HashMap of right table containing String: table Name and also Integer: id
     * @return           Returns a ResultSet of all columns within the junctionTable
     */
    // Each HashMap should accept the tableName and id that user would like to link
    public static ResultSet linkRowsManyToMany(HashMap<String, Integer> leftTable, HashMap<String, Integer> rightTable){
        String[] tableNames = getTableNameArray(leftTable.keySet().toArray()[0].toString(), rightTable.keySet().toArray()[0].toString());
        String junctionTableName =
                tableNames[0]
                        + "_" +
                        tableNames[1];
        return linkRowsManyToMany(junctionTableName, leftTable, rightTable);
    }
    /** executes Query; selecting all columns from the junction table where the targeted id
     *
     * @param junctionTableName String Name of junction table
     * @param leftTable         HashMap left table containing String: Table Name and Integer: id
     * @param rightTable        HashMap right table containing String: table Name and Integer: id
     * @return                  Returns a ResultSet consisting of all columns within the junctionTable
     *                          with the matching id
     */
    public static ResultSet linkRowsManyToMany(String junctionTableName,
                                               HashMap<String, Integer> leftTable,
                                               HashMap<String, Integer> rightTable){
        String[] tableNames = getTableNameArray(
                leftTable.keySet().toArray()[0].toString(),
                rightTable.keySet().toArray()[0].toString()
        );
        Object entry1 = leftTable.get(tableNames[0]);
        Object entry2 = rightTable.get(tableNames[1]);
        int id = PepperORM.addRow(junctionTableName, entry1, entry2);
        return HelperOrm.executeQuery(conn, "SELECT * FROM " + junctionTableName + " WHERE id=" + id);
    }











    @NotNull
    private static String[] getTableNameArray(String ...tableNames) {
        return Arrays.stream(tableNames).map(HelperOrm::sanitizeName
        ).toArray(String[]::new);
    }


}
