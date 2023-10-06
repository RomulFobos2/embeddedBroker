package com.tander.embeddedBroker.thread;

import com.tander.embeddedBroker.connector.ActiveMQConnectorBean;
import com.tander.embeddedBroker.service.DestinationService;
import com.tander.embeddedBroker.service.MessageService;
import com.tander.embeddedBroker.service.ProducerMessageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;

import javax.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;


/**
 * Поток, который генерирует текстовые сообщения и отправляет их в указанное место назначения (Destination).
 */
@Getter
@Setter
@AllArgsConstructor
public class SenderThreadBean implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SenderThreadBean.class);
    ActiveMQConnectorBean activeMQConnectorBean;
    String destinationName;
    Boolean isTransaction;
    Boolean isPersistent;
    Integer sessionMode;
    Boolean isQueue;

    @Override
    @PostConstruct
    public void run() {
        logger.info("Run sender...");
        while (true) {
            Session session = activeMQConnectorBean.createSession(isTransaction, sessionMode);
            try {
                Destination destination = DestinationService.getDestination(session, isQueue, destinationName);
                TextMessage textMessage = MessageService.createMessage(session);
                ProducerMessageService.sendMessageToBroker(session, textMessage, destination, isPersistent);
                Thread.sleep(5000);
            } catch (JMSException e) {
                logger.error("Error create attribute (Destination or TextMessage): ", e);
            } catch (InterruptedException e) {
                logger.error("Error in the thread: ", e);
            } finally {
                JmsUtils.closeSession(session);
            }
        }
    }
}