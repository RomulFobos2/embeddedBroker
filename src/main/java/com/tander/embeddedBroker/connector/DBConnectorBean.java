package com.tander.embeddedBroker.connector;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс представляет собой компонент для установки соединения с БД.
 * Параметры подключения (URL, имя пользователя и пароль).
 */
@Getter
@Setter
public class DBConnectorBean {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectorBean.class);

    private String url;
    private String username;
    private String password;
    private Connection connectionToDB; // Объект подключения к БД
    private DBConnectorBean(String url, String username, String password){
        this.url = url;
        this.username = username;
        this.password = password;
        initConnection();
    }

    private void initConnection() {
        try {
            this.connectionToDB = DriverManager.getConnection(url, username, password);
            logger.info("Database connection established successfully.");
        } catch (SQLException e) {
            logger.error("Failed to establish database connection: ", e);
            System.exit(-1);
        }
    }
}