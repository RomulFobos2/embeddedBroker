package com.tander.embeddedBroker;

import com.tander.embeddedBroker.thread.ConsumerThreadBean;
import com.tander.embeddedBroker.thread.SenderThreadBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
public class EmbeddedBrokerApplication {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        TaskExecutor taskExecutor = (TaskExecutor) context.getBean("taskExecutor");
        SenderThreadBean senderThreadBean = context.getBean(SenderThreadBean.class);
        ConsumerThreadBean consumerThreadBean = context.getBean(ConsumerThreadBean.class);
        for (int i = 0; i < consumerThreadBean.getCountConsumer(); i++) {
            taskExecutor.execute(senderThreadBean);
            taskExecutor.execute(consumerThreadBean);
        }
    }
}