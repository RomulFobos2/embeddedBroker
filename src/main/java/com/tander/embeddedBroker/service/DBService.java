package com.tander.embeddedBroker.service;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Setter
@AllArgsConstructor
public class DBService {
    private static final Logger logger = LoggerFactory.getLogger(DBService.class);

    /**
     * Проверяет существование таблицы в БД.
     *
     * @param connection Соединение с базой данных.
     * @param tableName  Имя таблицы для проверки.
     * @return true, если таблица существует, иначе false.
     */
    public static boolean tableExist(Connection connection, String tableName) {
        boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        } catch (SQLException e) {
            logger.error("Error while checking table existence: ", e);
            return false;
        }
        return tExists;
    }
}