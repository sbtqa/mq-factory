package ru.sbtqa.tag.mqimpl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.jms.*;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.mqfactory.exception.JmsException;
import ru.sbtqa.tag.mqfactory.interfaces.Mq;
import ru.sbtqa.tag.qautils.properties.Props;
import org.slf4j.Logger;

public class Jms implements Mq<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(Jms.class);
    private static final String TIMEOUT = "timeout";
    
    private static Connection connection;
    private String lastMsgId;

    public Jms(Connection connection) {
        Jms.connection = connection;
    }

    @Override
    public void sendRequestTo(String requestMsg, String queueName) throws JmsException {
        Session session = createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = createQueue(session, queueName);
        try {
            MessageProducer producer = session.createProducer(queue);
            Message request = session.createTextMessage(requestMsg);
            producer.send(request);
            lastMsgId = request.getJMSMessageID();
        } catch (JMSException ex) {
            throw new JmsException("Can't create session for message producer", ex);
        }

        LOG.info("Last message ID: " + lastMsgId);
        LOG.info("***Publishing message***");
        LOG.info("Message: \n {} \n Queue {}", requestMsg, queueName);
        sessionClose(session);
    }

    @Override
    public Object getAnswerFrom(String queueName) throws JmsException {
        Message message;
        Session session = createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = createQueue(session, queueName);
        QueueReceiver receiver;
        try {
            receiver = (QueueReceiver) session.createConsumer(queue, "JMSCorrelationID='" + lastMsgId + "'");
        } catch (JMSException ex) {
            throw new JmsException("Can't create consumer for messages", ex);
        }
        if (Props.get(TIMEOUT).isEmpty()) {
            try {
                message = receiver.receive();
            } catch (JMSException ex) {
                throw new JmsException("Can't receive message", ex);
            }
        } else {
            try {
                message = receiver.receive(Long.getLong(Props.get(TIMEOUT)));
            } catch (JMSException ex) {
                throw new JmsException("Can't receive message", ex);
            }
        }
        sessionClose(session);
        lastMsgId = "";
        return message;
    }

    @Override
    public List<Object> getMessagesFromByParam(String queueName, String paramName, String paramValue) throws JmsException {
        List<Object> messages = new ArrayList<>();
        QueueBrowser browser;
        Enumeration eQueue;
        Session session = createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = createQueue(session, queueName);
        try {
            browser = session.createBrowser(queue, paramName + "='" + paramValue + "'");
            eQueue = browser.getEnumeration();
        } catch (JMSException ex) {
            throw new JmsException("Can't open browser session", ex);
        }
        long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < Long.valueOf(Props.get(TIMEOUT)) && eQueue.hasMoreElements()) {
                try {
                    Message message = (Message) eQueue.nextElement();
                    QueueReceiver receiver = (QueueReceiver) session.createConsumer(queue, "JMSMessageID='" + message.getJMSMessageID() + "'");
                    Message receivedMessage = receiver.receive();
                    messages.add(receivedMessage);
                } catch (JMSException ex) {
                    throw new JmsException("Can't get messages from queue", ex);
                }
            }
        sessionClose(session);
        return messages;
    }

    @Override
    public List<Object> browseAllMessagesFrom(String queueName) throws JmsException {
        List<Object> messages = new ArrayList<>();
        Session session = createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue;
        Enumeration eQueue;
        try {
            queue = session.createQueue(queueName);
            eQueue = session.createBrowser(queue).getEnumeration();
        } catch (JMSException ex) {
            throw new JmsException("Can't create browser session", ex);
        }
        while (eQueue.hasMoreElements()) {
            Message message = (Message) eQueue.nextElement();
            messages.add(message);
        }
        sessionClose(session);
        return messages;
    }

    @Override
    public String getMessageText(Message message) throws JmsException {
        try {
            return ((TextMessage) message).getText();
        } catch (JMSException ex) {
            throw new JmsException("Can't get text from message", ex);
        }
    }

    @Override
    public String getMessageProperty(Message message, String property) throws JmsException {
        try {
            return message.getStringProperty(property);
        } catch (JMSException ex) {
            throw new JmsException("Can't get property from message", ex);
        }
    }

    private Session createSession(boolean bln, int i) throws JmsException {
        try {
            return connection.createSession(bln, i);
        } catch (JMSException ex) {
            throw new JmsException("Can't get session", ex);
        }
    }

    private void sessionClose(Session session) throws JmsException {
        try {
            session.close();
        } catch (JMSException ex) {
            throw new JmsException("Can't close session", ex);
        }
    }

    private Queue createQueue(Session session, String queueName) throws JmsException {
        try {
            return session.createQueue(queueName);
        } catch (JMSException ex) {
            throw new JmsException("Can't create queue", ex);
        }
    }

}
