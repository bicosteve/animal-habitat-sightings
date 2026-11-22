package cz.fit.mit.Animal_Habitat_Sightings;

import oracle.jdbc.pool.OracleDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class AnimalHabitatSightingsApplication {

	public static void main(String[] args)
	{
		try (Connection conn = getConnection()) {

			if (conn.isValid(5)) {
				System.out.println("Oracle database connection successful!");
			}

		} catch (SQLException e) {
			System.err.println("Oracle connection failed:");
		}

		SpringApplication.run(AnimalHabitatSightingsApplication.class, args);
	}

	public static Connection getConnection() throws SQLException {
		OracleDataSource oracleDataSource = new OracleDataSource();

		oracleDataSource.setURL("jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb");
		oracleDataSource.setUser("xvychor00");
		oracleDataSource.setPassword("NgNSXNpC");

		return oracleDataSource.getConnection();
	}
}
