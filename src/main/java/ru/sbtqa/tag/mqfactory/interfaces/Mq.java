/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sbtqa.tag.mqfactory.interfaces;

import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.sbtqa.tag.mqfactory.exception.MqException;



public interface Mq<T> {

    /**
     * Send request to queue
     *
     * @param requestMsg message for sending
     * @param queueName Queue name
     * @throws ru.sbtqa.tag.mqfactory.exception.MqException if can't create
     * session
     */
    public void sendRequestTo(String requestMsg, String queueName) throws MqException;

    /**
     * Get answer from queue
     *
     * @param queueName queue name for connect
     * @return answer from queue as Object
     * @throws ru.sbtqa.tag.mqfactory.exception.MqException if can't receive
     * message or if can't create consumer for message
     *
     */
    public Object getAnswerFrom(String queueName) throws MqException;

    /**
     * Get text body from received Message or Record
     *
     * @param message message
     * @return text from message as String
     * @throws ru.sbtqa.tag.mqfactory.exception.MqException if can't get text
     * from message
     */
    public String getMessageText(T message) throws MqException;

    /**
     * Get message property
     *
     * @param message message for sending
     * @param property property for return
     * @return property of the message as Object
     * @throws ru.sbtqa.tag.mqfactory.exception.MqException if can't get message
     * property
     */
    public Object getMessageProperty(T message, String property) throws MqException;

    /**
     * Send request to Kafka topic with key value
     *
     * @param requestMsg message for sending
     * @param key key value
     * @param topicName topic name
     */
    public void sendRequestTo(String requestMsg, String key, String topicName);

    /**
     * Get appropriate number of the last messages from Kafka topic in
     * corresponding partition label
     *
     * @param topicName topic name
     * @param partiton partition label
     * @param numberOfMessages number of message from the end of partition
     * @return appropriate number of messages from the end of partition as List
     * of Consumer Records
     */
    public List<ConsumerRecord<String, String>> getLastMessagesInPartition(String topicName, int partiton, int numberOfMessages);

    /**
     * Get message from queue by parameter and parameter's value
     *
     * @param queueName queue name
     * @param paramName parameter name
     * @param paramValue parameter value
     * @return messages related to parameters
     * @throws ru.sbtqa.tag.mqfactory.exception.MqException if can't get message
     * from queue or can't establish browser session
     */
    public List<Object> getMessagesFromByParam(String queueName, String paramName, String paramValue) throws MqException;

    /**
     * Browse all messages from queue
     *
     * @param queueName
     * @return all messages in queue as List of object
     * @throws ru.sbtqa.tag.mqfactory.exception.MqException if can't create
     * browse session
     */
    public List<Object> browseAllMessagesFrom(String queueName) throws MqException;

    /**
     * Browse all messages in Kafka topic at the specified partition
     *
     * @param topicName topic name
     * @param partiton partition label
     * @return List of ConsumerRecords
     */
    public List<ConsumerRecord<String, String>> browseAllMessagesFromPartition(String topicName, int partiton);

}
