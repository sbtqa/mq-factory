package ru.sbtqa.tag.mqimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.mqfactory.exception.KafkaException;
import ru.sbtqa.tag.mqfactory.interfaces.Mq;
import ru.sbtqa.tag.qautils.properties.Props;
import org.slf4j.Logger;
import ru.sbtqa.tag.mqfactory.exception.MqException;

public class Kafka implements Mq<ConsumerRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(Kafka.class);
    private static final String TIMEOUT = "timeout";

    private Properties producerProperties;
    private Properties consumerProperties;

    public Kafka(Properties producerProperties, Properties consumerProperties) {
        this.producerProperties = producerProperties;
        this.consumerProperties = consumerProperties;
    }

    @Override
    public void sendRequestTo(String requestMsg, String topicName) throws KafkaException {
        Producer<String, String> producer = new KafkaProducer<>(producerProperties);
        producer.send(new ProducerRecord<String, String>(topicName, requestMsg));
        producer.close();
    }

    @Override
    public void sendRequestTo(String requestMsg, String key, String topicName) {
        Producer<String, String> producer = new KafkaProducer<>(producerProperties);
        producer.send(new ProducerRecord<String, String>(topicName, key, requestMsg));
        producer.close();
    }

    @Override
    public Object getAnswerFrom(String queueName) throws KafkaException {
        return getLastMessagesInPartition(queueName, 0, 0);
    }

    @Override
    public List<ConsumerRecord<String, String>> getLastMessagesInPartition(String topicName, int partiton, int numberOfMessages) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        TopicPartition tp = new TopicPartition(topicName, partiton);
        List<String> topicList = new ArrayList<>();
        topicList.add(topicName);
        consumer.assign(Arrays.asList(tp));
        consumer.seek(tp, consumer.position(tp) - numberOfMessages);
        ConsumerRecords<String, String> records = consumer.poll(Long.valueOf(Props.get(TIMEOUT)));
        for(ConsumerRecord<String, String> rec :records){
            buffer.add(rec);
        }
        consumer.close();
        return buffer;
    }

    @Override
    public List<ConsumerRecord<String, String>> browseAllMessagesFromPartition(String topicName, int partiton) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        TopicPartition tp = new TopicPartition(topicName, partiton);
        List<String> topicList = new ArrayList<>();
        topicList.add(topicName);
        consumer.assign(Arrays.asList(tp));
        consumer.seekToBeginning(tp);
        ConsumerRecords<String, String> records = consumer.poll(Long.valueOf(Props.get(TIMEOUT)));
        for(ConsumerRecord<String, String> rec :records){
            buffer.add(rec);
        }
        consumer.close();
        return buffer;
    }

    @Override
    public String getMessageText(ConsumerRecord message) throws KafkaException {
        return message.value().toString();
    }

    @Override
    public String getMessageProperty(ConsumerRecord message, String property) throws KafkaException {
        String propertyValue;
        try {
            switch (property) {
                case "topic":
                    propertyValue = message.topic();
                    break;
                case "partition":
                    propertyValue = String.valueOf(message.partition());
                    break;
                case "offset":
                    propertyValue = String.valueOf(message.offset());
                    break;
                case "key":
                    propertyValue = message.key().toString();
                    break;
                default:
                    throw new KafkaException("Kafka records dont' have such property: " + property);
            }
        } catch (NullPointerException ex) {
            LOG.info("Can't find key value or it's equal null", ex);
            propertyValue = "null";
        }
        return propertyValue;
    }

    @Override
    public List<Object> getMessagesFromByParam(String queueName, String paramName, String paramValue) throws MqException {
        throw new UnsupportedOperationException("Not supported in this realization.");
    }

    @Override
    public List<Object> browseAllMessagesFrom(String queueName) throws MqException {
        throw new UnsupportedOperationException("Not supported in this realization."); 
    }

}
