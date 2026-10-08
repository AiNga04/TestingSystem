package org.vti.jamie.com.project_spring_boot;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnectionTest {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/TestingSystem?useSSL=false";
        String username = "root";
        String password = "123456";

        try (Connection connection =
                     DriverManager.getConnection(
                             url, username, password)) {

            System.out.println("CONNECTED SUCCESSFULLY!");
            System.out.println(
                    "Database: " +
                            connection.getMetaData().getDatabaseProductName()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}