package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

    public static Connection getConnection() throws SQLException {
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        String db = System.getenv("DB_NAME");
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");

        // Fallback für lokale Tests ohne Docker
        if (host == null) host = "localhost";
        if (port == null) port = "5432";
        if (db == null) db = "mrp_db";
        if (user == null) user = "mrp_user";
        if (pass == null) pass = "mrp_pass";

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
        System.out.println("Connecting to DB: " + url);

        return DriverManager.getConnection(url, user, pass);
    }
}