package com.tander.embeddedBroker.thread;

import com.tander.embeddedBroker.connector.ActiveMQConnectorBean;
import com.tander.embeddedBroker.porcessing.MessageProcessBean;
import com.tander.embeddedBroker.service.DestinationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;


/**
 * Класс ConsumerThreadBean поток, который слушает сообщения из указанного места назначения (Destination)
 * и обрабатывает. Полученные текстовые сообщения сохраняются в базе данных.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ConsumerThreadBean implements MessageListener, Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ConsumerThreadBean.class);

    private final ActiveMQConnectorBean activeMQConnectorBean;
    private final MessageProcessBean messageProcessBean; // Объект для обработки и сохранения сообщений в базе данных

    private final String destinationName; // Имя места назначения, откуда читаются сообщения
    private final Boolean isTransaction; // Флаг, указывающий, следует ли использовать транзакции для чтения сообщений
    private final Integer sessionMode; // Режим сессии при чтении сообщений
    private final Boolean isQueue; // Флаг, указывающий, является ли место назначения очередью (true) или топиком (false)
    private final Integer countConsumer; //Кол-во слушателей для очереди
    private Session session;


    @Override
    public void onMessage(Message message) {
            logger.info("Try reading message...");
            try {
                activeMQConnectorBean.getConnection().start();
                if(message instanceof TextMessage && messageProcessBean.writeMessageToDB((TextMessage) message)){
                    if (session.getTransacted()){
                        logger.error("Commit completed");
                        session.commit();
                    }
                    else {
                        if (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE | session.getAcknowledgeMode() == Session.DUPS_OK_ACKNOWLEDGE) {
                            message.acknowledge();
                        }
                    }
                }
                else {
                    logger.error("Can't write to DB");
                    if (message == null){
                        logger.error("Message is null.");
                    }
                    if (session.getTransacted()){
                        logger.error("Rollback completed");
                        session.rollback();
                    }
                }
            } catch (JMSException e) {
                logger.error("Error create attribute (Destination or TextMessage): ", e);
            }
    }

    @Override
    public void run() {
       try {
           this.session = activeMQConnectorBean.createSession(isTransaction, sessionMode);
           activeMQConnectorBean.getConnection().start();
           Destination destination = DestinationService.getDestination(session, isQueue, destinationName);
           MessageConsumer messageConsumer = session.createConsumer(destination);
           messageConsumer.setMessageListener(this);
           logger.info("Consumer start");
       }
       catch (Exception e){
           logger.error("Error in the run method: ", e);
       }
    }
}