package com.tander.embeddedBroker.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;

import javax.jms.*;


/**
 * Утилитарный класс для отправки текстовых JMS-сообщений в брокер.
 */
@Getter
@Setter
@AllArgsConstructor
public class ProducerMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ProducerMessageService.class);


    /**
     * Отправляет текстовое сообщение в указанное место назначения (Destination) в сессии.
     *
     * @param session       JMS-сессия, используемая для отправки сообщения.
     * @param textMessage   Текстовое JMS-сообщение, которое требуется отправить.
     * @param destination   Место назначения, куда будет отправлено сообщение.
     * @param isPersistent  Указывает, следует ли сохранять сообщение
     */
    public static void sendMessageToBroker(Session session, TextMessage textMessage, Destination destination, boolean isPersistent) {
        logger.info("Try send message (Persistent: " + isPersistent + ")...");
        MessageProducer messageProducer = null;
        try {
            messageProducer = session.createProducer(destination);
            messageProducer.setDeliveryMode(isPersistent ? 2 : 1);
            if (session.getTransacted()) {
                sendTransactionMessage(session, messageProducer, textMessage);
            } else {
                sendNonTransactionMessage(session, messageProducer, textMessage);
            }
        } catch (JMSException e) {
            logger.error("Error when creating attributes for sending a message: ", e);
        }
        finally {
            JmsUtils.closeMessageProducer(messageProducer);
        }
    }

    //Отправляет сообщение в транзакционной сессии.
    public static void sendTransactionMessage(Session session, MessageProducer messageProducer, TextMessage textMessage) {
        try {
            messageProducer.send(textMessage);
            logger.info("Message send successfully.");
            session.commit();
        } catch (JMSException e) {
            logger.error("Error while send message: ", e);
            try {
                session.rollback();
            } catch (JMSException ex) {
                logger.error("Error while rollback: ", e);
            }
        }
    }

    //Отправляет сообщение в не транзакционной сессии.
    public static void sendNonTransactionMessage(Session session, MessageProducer messageProducer, TextMessage textMessage) {
        try {
            messageProducer.send(textMessage);
            logger.info("Message send successfully.");
        } catch (JMSException e) {
            logger.error("Error while send message: ", e);
        }
    }
}