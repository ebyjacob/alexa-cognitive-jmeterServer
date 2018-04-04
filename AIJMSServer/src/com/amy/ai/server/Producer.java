package com.amy.ai.server;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Producer {
	


  private final Log log = LogFactory.getLog(getClass());
  
  private Connection connection;
  private Session session;
  private MessageProducer producer;
  private MessageConsumer consumer;
  
  private String response;

  public void init() throws Exception {
    // set 'er up
    ActiveMQConnectionFactory connectionFactory = 
    //  new ActiveMQConnectionFactory("tcp://localhost:61616");
    new ActiveMQConnectionFactory("tcp://ec2-52-213-61-56.eu-west-1.compute.amazonaws.com:61616");
    
    connection = connectionFactory.createConnection();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    // create our request and response queues
    Queue request = session.createQueue("request.queue");
    Queue response = session.createQueue("response.queue");
    // and attach a consumer and producer to them
    producer = session.createProducer(request);
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    consumer = session.createConsumer(response);
    // and start your engines...
    connection.start();
  }

  public void destroy() throws Exception {
    session.close();
    connection.close();
    log.info("Closed client connection ");
  }
  
  public String sendMessage(String messageText) throws Exception {
    try {
      log.info("Client: Send request [" + messageText + "]");
      TextMessage message = session.createTextMessage(messageText);
      String messageId = UUID.randomUUID().toString();
      message.setJMSCorrelationID(messageId);
      producer.send(message);
      Message response = consumer.receive();
      String responseText = ((TextMessage) response).getText(); 
      return responseText;
    } catch (JMSException e) {
      log.error("JMS Exception on client", e);
    }
    return response;
  }
}