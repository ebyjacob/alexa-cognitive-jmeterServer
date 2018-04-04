package com.amy.ai.server;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Consumer implements MessageListener {

  private final Log log = LogFactory.getLog(getClass());
  
  private Connection connection;
  private Session session;
  private MessageConsumer consumer;
  private MessageProducer producer;
  String ResponseMsg="";
  
  public void init() throws Exception {
    // set 'er up
    ActiveMQConnectionFactory connectionFactory = 
      new ActiveMQConnectionFactory("tcp://localhost:61616");
    connection = connectionFactory.createConnection();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    // create our request and response queues
    Queue request = session.createQueue("request.queue");
    Queue response = session.createQueue("response.queue");
    // and attach a consumer and producer to them
    consumer = session.createConsumer(request);
    consumer.setMessageListener(this);
    producer = session.createProducer(response);
    // start your engines...
    connection.start();
  }
  
  public void destroy() throws Exception {
    session.close();
    connection.close();
  }

  public void onMessage(Message message)  {
    try {
      if (message instanceof TextMessage) {
        String messageText = ((TextMessage) message).getText();
        String commandParams[]=messageText.split("&");
		String scenarioName="";
	    String Domain="";
	    String Command="";
	    int numLoops=0;
		int numThreads=0;
	    int RampUp=0;
        
 /**************************************************************/
        
		for(int i=0;i<commandParams.length;i++)
		{
			
			//log.info("Name is "+commandParams[i].split("=")[0]);
			//log.info("Value is "+commandParams[i].split("=")[1]);
		if(commandParams[i].split("=")[0].equals("scenarioName") )
			scenarioName=commandParams[i].split("=")[1];
		else if(commandParams[i].split("=")[0].equals("Domain") )
			Domain=commandParams[i].split("=")[1];
		else if(commandParams[i].split("=")[0].equals("numLoops") )
			numLoops=Integer.valueOf(commandParams[i].split("=")[1]);
		else if(commandParams[i].split("=")[0].equals("numThreads") )
			numThreads=Integer.valueOf(commandParams[i].split("=")[1]);
		else if(commandParams[i].split("=")[0].equals("RampUp") )
			RampUp=Integer.valueOf(commandParams[i].split("=")[1]);
		else if(commandParams[i].split("=")[0].equals("Command") )
			Command=commandParams[i].split("=")[1];
		}
		
		log.debug("Command sent from producer is :: "+ messageText);
		log.debug("Command value RampUp = " + scenarioName);
		log.debug("Command value RampUp = " + Domain);
		log.debug("Command value numLoops =" +  numLoops);
		log.debug("Command value numThreads = " +numThreads);
		log.debug("Command value RampUp = " + RampUp);
		log.debug("Command value Command = " + Command);
		
		if(Command.equals("Start Test"))
		{
			log.info("Creating Jmeter test suite");
			SmartJMeterAI SJAI = new SmartJMeterAI();
			SJAI.setScenarioName(scenarioName);
			SJAI.setDomain(Domain);
			SJAI.setNumLoops(numLoops);
			SJAI.setNumThreads(numThreads);
			SJAI.setRampUp(RampUp);
			Thread a = new Thread(SJAI);
			a.setName("aiJrunner");
			//a.setDaemon(true);
			a.start();
			//a.stop();
			log.info(" Jmeter test completed");		
			ResponseMsg = "Processed command " + Command;
		}
		
		
		/******************************************************/        
        
        log.debug("Server: request recieved [" + messageText + "]");
        Message responseMessage = 
          session.createTextMessage(ResponseMsg);
        if (message.getJMSCorrelationID() != null) {
          // pass it through
        	log.debug("Server: Correlation ID of Message Recieved is"+ message.getJMSCorrelationID());
          responseMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        }
        producer.send(responseMessage);
      }
    } catch (JMSException e) {
      log.error(e);
    }
  }
}