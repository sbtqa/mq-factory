package ru.sbtqa.tag.mqfactory;

import java.util.Properties;
import javax.jms.Connection;
import ru.sbtqa.tag.mqimpl.Jms;
import ru.sbtqa.tag.mqimpl.Kafka;
import ru.sbtqa.tag.mqfactory.interfaces.Mq;
import ru.sbtqa.tag.mqfactory.error.MqError;

/**
 *
 * @author sbt-polosov-va
 */
public class MqFactory {

    public static final String MQ_TYPE = "mq.type";
    public static final String JMS_CONNECTION = "jms.connection";
    public static final String PRODUCER_PROPERTIES = "producer.properties";
    public static final String CONSUMER_PROPERTIES = "consumer.properties";

    private MqFactory() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Get Mq instance for
     *
     * @param connProps connection properties must have - connection.type and
     * jms.connection for jms instances - producer.properties and
     * consumer.properties for kafka instances
     *
     * Connection should be implement and return as javax.jms.Connection and
     * include all necessary connection properties in it. The number of
     * parameters you define by yourself.
     *
     * Connection types that supports now is: - websphere - activeMq - kafka
     *
     * @return mq instance as Mq
     */
    public static Mq getMq(Properties connProps) {
        Mq mqRealisation;
        String connectionType;
        if (!connProps.containsKey(MQ_TYPE)) {
            throw new MqError("Mq type didn't set in property " + MQ_TYPE);
        }
        connectionType = (String) connProps.get(MQ_TYPE);

        switch (connectionType) {
            case "websphere":
            case "activeMq":
                mqRealisation = jmsConnection(connProps);
                break;
            case "kafka":
                mqRealisation = kafkaConnection(connProps);
                break;
            default:
                throw new UnsupportedOperationException("Request method " + connectionType + " is not support");
        }
        return mqRealisation;
    }

    private static Mq jmsConnection(Properties connProps) {
        if (!connProps.containsKey(JMS_CONNECTION)) {
            throw new MqError("Connection didn't set in property " + JMS_CONNECTION);
        }
        return new Jms((Connection) connProps.get(JMS_CONNECTION));
    }

    private static Mq kafkaConnection(Properties connProps) {
        if (!connProps.containsKey(PRODUCER_PROPERTIES)) {
            throw new MqError("Producer properties didn't set in property " + PRODUCER_PROPERTIES);
        }
        if (!connProps.containsKey(CONSUMER_PROPERTIES)) {
            throw new MqError("Consumer properties didn't set in property " + CONSUMER_PROPERTIES);
        }
        return new Kafka((Properties) connProps.get(PRODUCER_PROPERTIES), (Properties) connProps.get(CONSUMER_PROPERTIES));
    }
}
