package com.tander.embeddedBroker.service;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Класс-утилита для создания текстовых JMS-сообщений.
 */
@Getter
@Setter
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    /**
     * Создает и возвращает JMS-сообщение с уникальным текстом и темой, представляющей текущую дату и время.
     *
     * @param sessionProducer JMS-сессия, используемая для создания сообщения.
     * @return JMS-сообщение с уникальным текстом и темой, представляющей текущую дату и время.
     * @throws JMSException если произошла ошибка при создании сообщения.
     */
    public static TextMessage createMessage(Session sessionProducer) throws JMSException {
        String randomBodyMessage = String.valueOf(UUID.randomUUID()).replace("-", "").substring(0, 16);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String themeMessage = dateFormat.format(new Date());
        TextMessage message = sessionProducer.createTextMessage(randomBodyMessage);
        message.setStringProperty("theme", themeMessage);
        logger.info("Create message. Text: " + randomBodyMessage + " ; Theme: " + themeMessage);
        return message;
    }
}
