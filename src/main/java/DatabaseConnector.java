import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatabaseConnector {
    private String database;
    private String user;
    private String password;

    public DatabaseConnector(String database, String user, String password) {
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, user, password);
    }

    /**
     * Opens a database connection and returns a set of found tables. Will
     * return an empty set if there are no results.
     *
     * @return set of tables
     */
    public Set<String> getTables(Connection db) throws SQLException {
        Set<String> tables = new HashSet<>();

        // Create statement and close when done.
        // Database connection will be closed elsewhere.
        try (Statement sql = db.createStatement();) {
            if (sql.execute("SHOW TABLES;")) {
                ResultSet results = sql.getResultSet();

                while (results.next()) {
                    tables.add(results.getString(1));
                }
            }
        }

        return tables;
    }

    /**
     * Opens a database connection, executes a simple statement, and closes
     * the database connection.
     *
     * @return true if all operations successful
     */
    public boolean testConnection() {
        boolean okay = false;

        // Open database connection and close when done
        try (Connection db = getConnection();) {
            System.out.println("Executing SHOW TABLES...");
            Set<String> tables = getTables(db);

            if (tables != null) {
                System.out.print("Found " + tables.size() + " tables: ");
                System.out.println(tables);

                okay = true;
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return okay;
    }

}
