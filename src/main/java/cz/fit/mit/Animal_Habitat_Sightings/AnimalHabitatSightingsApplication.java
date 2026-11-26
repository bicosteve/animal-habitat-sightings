package cz.fit.mit.Animal_Habitat_Sightings;


import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class AnimalHabitatSightingsApplication {

    // Populated at startup from application.yaml (which reads values from .env).
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;

    public static void main(String[] args) {
        SpringApplication.run(AnimalHabitatSightingsApplication.class, args);

        try (Connection conn = getConnection()) {
            if (conn.isValid(5)) {
                System.out.println("Oracle database connection successful!");
            }
        } catch (SQLException e) {
            System.err.println("Oracle connection failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        OracleDataSource oracleDataSource = new OracleDataSource();

        oracleDataSource.setURL(dbUrl);
        oracleDataSource.setUser(dbUsername);
        oracleDataSource.setPassword(dbPassword);

        return oracleDataSource.getConnection();
    }

    /**
     * Copies the Oracle credentials from Spring configuration into the static
     * fields used by {@link #getConnection()}. The property placeholders in
     * application.yaml (${DB_URL}, ${DB_USERNAME}, ${DB_PASSWORD}) are resolved
     * from the .env file by the spring-dotenv library.
     */
    @Component
    static class DatabaseProperties {
        DatabaseProperties(
                @Value("${oracle.datasource.url}") String url,
                @Value("${oracle.datasource.username}") String username,
                @Value("${oracle.datasource.password}") String password) {
            AnimalHabitatSightingsApplication.dbUrl = url;
            AnimalHabitatSightingsApplication.dbUsername = username;
            AnimalHabitatSightingsApplication.dbPassword = password;
        }
    }
}
