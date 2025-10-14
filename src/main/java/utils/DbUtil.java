package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Temporär Hardcode für Test
            String user = "mrp_user";
            String pass = "mrp_pass";
            String db = "mrp_db";
            String host = "localhost";
            String port = "5432";

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            System.out.println("Connecting to DB: " + url);
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }
}
