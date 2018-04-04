package com.amy.ai.server;

public class ProducerServer {

	static String scenarioName="";
	static String Domain="";
	static String Task = "";
	static String Command="";    
	static int numLoops=0;
	static int numThreads=0;
	static int RampUp=0;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			scenarioName = "AI_JMeter_Test_For_Demo";
			Domain="www.cognizant.com/en-uk/";
			numLoops=10;
			numThreads=10;
			RampUp=1;
			Command="Start Test";
			Task="scenarioName="+scenarioName + "&Domain="+ Domain+"&numLoops="+numLoops +"&numThreads="+numThreads+"&RampUp="+RampUp+"&Command="+Command;
					
			Producer psc =  new Producer();
			psc.init();
			psc.sendMessage(Task);
			psc.destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
