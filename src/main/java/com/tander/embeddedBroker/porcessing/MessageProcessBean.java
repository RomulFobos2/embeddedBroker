package com.tander.embeddedBroker.porcessing;

import com.tander.embeddedBroker.connector.DBConnectorBean;
import com.tander.embeddedBroker.service.DBService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс отвечает за подготовку таблиц в которые будут записываться сообщения
 * и непосредственно запись сообщений в БД.
 */
@Getter
@Setter
public class MessageProcessBean {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessBean.class);

    DBConnectorBean dbConnectorBean;

    String messageTableName;
    String propertiesTableName;
    String sqlCreateMessageTable;
    String sqlCreatePropertiesTable;
    String sqlInsertPropertiesTable;
    String sqlInsertMessageTable;

    /**
     * Конструктор класса, инициализирует объект и создает необходимые таблицы (если их нет) в базе данных.
     *
     * @param dbConnectorBean          объект для подключения к БД
     * @param messageTableName         название таблицы для хранения тела сообщений
     * @param propertiesTableName      название таблицы для хранения заголовков сообщений
     * @param sqlCreateMessageTable    SQL-запрос для создания таблицы сообщений
     * @param sqlCreatePropertiesTable SQL-запрос для создания таблицы заголовков сообщений
     * @param sqlInsertPropertiesTable SQL-запрос для вставки записи в таблицу заголовков сообщений
     * @param sqlInsertMessageTable    SQL-запрос для вставки записи в таблицу сообщений
     */
    public MessageProcessBean(DBConnectorBean dbConnectorBean, String messageTableName, String propertiesTableName, String sqlCreateMessageTable, String sqlCreatePropertiesTable, String sqlInsertPropertiesTable, String sqlInsertMessageTable) {
        this.dbConnectorBean = dbConnectorBean;
        this.messageTableName = messageTableName;
        this.propertiesTableName = propertiesTableName;
        this.sqlCreateMessageTable = sqlCreateMessageTable;
        this.sqlCreatePropertiesTable = sqlCreatePropertiesTable;
        this.sqlInsertPropertiesTable = sqlInsertPropertiesTable;
        this.sqlInsertMessageTable = sqlInsertMessageTable;
        createTable();
    }

    public void createTable() {
        Connection connection = dbConnectorBean.getConnectionToDB();
        try (Statement statement = connection.createStatement()) {
            if (!DBService.tableExist(connection, propertiesTableName)) {
                statement.executeUpdate(sqlCreatePropertiesTable);
            }
            if (!DBService.tableExist(connection, messageTableName)) {
                statement.executeUpdate(sqlCreateMessageTable);
            }
        } catch (SQLException e) {
            logger.error("Error while create table: ", e);
        }
    }

    /**
     * Записывает сообщение в БД, включая его заголовки.
     *
     * @param textMessage сообщение, которое нужно записать в БД
     * @return true, если сообщение было успешно записано, в противном случае - false
     */
    public boolean writeMessageToDB(TextMessage textMessage) {
        Connection connection = dbConnectorBean.getConnectionToDB();
        try (Statement statement = connection.createStatement()){
            String theme = textMessage.getStringProperty("theme");
            String sqlCommandForPropertiesTbl = sqlInsertPropertiesTable.replace("{VAR_1}", theme);
            statement.executeUpdate(sqlCommandForPropertiesTbl, Statement.RETURN_GENERATED_KEYS);

            long generatedKey;
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getLong(1);
                } else {
                    throw new SQLException("Failed to retrieve generated key.");
                }
            }
            String text = textMessage.getText();
            String sqlCommandForMessageTbl = sqlInsertMessageTable.replace("{VAR_1}", text)
                    .replace("{VAR_2}", String.valueOf(generatedKey));
            statement.executeUpdate(sqlCommandForMessageTbl);
            logger.info("Write message completed. Theme: " + theme + " ; Text: " + text);
        } catch (SQLException | JMSException e) {
            logger.error("Error write message: ", e);
            return false;
        }
        return true;
    }
}
