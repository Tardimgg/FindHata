package com.company;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.*;
import java.util.Map;

@Slf4j
public class DatabaseCreator {

    public static void init() {
        InputStream inputStream = DatabaseCreator.class.getResourceAsStream("/application.yml");

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);

        String dbPath = ((Map) ((Map) data.get("spring")).get("datasource")).get("root-url").toString();

        String dbUsername = ((Map) ((Map) data.get("spring")).get("datasource")).get("username").toString();
        String dbPassword = ((Map) ((Map) data.get("spring")).get("datasource")).get("password").toString();

        String dbName = ((Map) ((Map) data.get("spring")).get("datasource")).get("data-base-name").toString();

        Connection connection = null;
        Statement statement = null;
        try {
            log.debug("Creating database if not exist...");
            connection = DriverManager.getConnection(dbPath, dbUsername, dbPassword);
            statement = connection.createStatement();
            statement.executeQuery(new StringBuilder("SELECT count(*) FROM pg_database WHERE datname = ")
                    .append("'")
                    .append(dbName)
                    .append("'")
                    .toString());

            ResultSet resultSet = statement.getResultSet();
            resultSet.next();
            int count = resultSet.getInt(1);

            if (count <= 0) {
                statement.executeUpdate("CREATE DATABASE " + dbName);
                log.debug("Database created.");
            } else {
                log.debug("Database already exist.");
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                    log.debug("Closed Statement.");
                }
                if (connection != null) {
                    log.debug("Closed Connection.");
                    connection.close();
                }
            } catch (SQLException e) {
                log.error(e.toString());
            }
        }
    }
}
