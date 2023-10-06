package com.tander.embeddedBroker.service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

public class DestinationService {

    /**
     * Создает и возвращает Destination в зависимости от типа (очередь или тема).
     *
     * @param session        сессия, используемая для создания Destination.
     * @param isQueue        Флаг, указывающий, является ли Destination очередью (true) или темой (false).
     * @param destinationName Имя Destination.
     * @return JMS Destination (очередь или тема) в соответствии с переданными параметрами.
     * @throws JMSException если произошла ошибка при создании Destination.
     */
    public static Destination getDestination(Session session, Boolean isQueue, String destinationName) throws JMSException {
        return isQueue ? session.createQueue(destinationName) : session.createTopic(destinationName);
    }
}
