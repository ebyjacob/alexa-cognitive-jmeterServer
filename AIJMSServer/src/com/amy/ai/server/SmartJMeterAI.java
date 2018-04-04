package com.amy.ai.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.java.control.gui.JavaTestSamplerGui;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.log4j.Logger;
/**
*
* @author Eby Jacob
*/

public class SmartJMeterAI implements Runnable{
	static Logger log = Logger.getLogger(SmartJMeterAI.class);
    //File jmeterHome = new File("/opt/jmeter");
	File jmeterHome = new File("/Users/ebyjacob/Desktop/jmeter");
    String slash = System.getProperty("file.separator");
    String scenarioName="";
    String domain="";
    

	int numLoops=0;
	int numThreads=0;
    int rampUp=0;
    
    public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
 

	public int getNumLoops() {
		return numLoops;
	}

	public void setNumLoops(int numLoops) {
		this.numLoops = numLoops;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public int getRampUp() {
		return rampUp;
	}

	public void setRampUp(int rampUp) {
		this.rampUp = rampUp;
	}
    
    	
	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public void run() {
		// TODO Auto-generated method stub
        try {
        	log.info("AI Called");
	        if (jmeterHome.exists()) {
	            File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
	            if (jmeterProperties.exists()) {
	                //JMeter Engine
	                StandardJMeterEngine jmeter = new StandardJMeterEngine();
	                jmeter.reset();
	                //JMeter initialization (properties, log levels, locale, etc)
	                JMeterUtils.setJMeterHome(jmeterHome.getPath());
	                JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
	                JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
	                JMeterUtils.initLocale();

	                
	                // JMeter Test Plan, basically JOrphan HashTree
	                HashTree testPlanTree = new HashTree();

	                // First HTTP Sampler - open uttesh.com
	                HTTPSamplerProxy aiSampler = new HTTPSamplerProxy();
	                aiSampler.setDomain(this.domain);
	                aiSampler.setPort(80);
	                aiSampler.setAutoRedirects(true);
	                aiSampler.setPath("/");
	                aiSampler.setMethod("GET");
	                aiSampler.setName("Opening "+ this.getRampUp());
	                aiSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
	                aiSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
	                aiSampler.addArgument("SleepTime", "1000");
	                aiSampler.addArgument("SleepMask", "0x33F");

	                // Loop Controller
	                LoopController loopController = new LoopController();
	                loopController.setLoops(this.getNumLoops());
	                loopController.setFirst(true);
	                loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
	                loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
	                loopController.initialize();

	                // Thread Group
	                ThreadGroup threadGroup = new ThreadGroup();
	                threadGroup.setName(scenarioName);
	                threadGroup.setNumThreads(this.getNumThreads());
	                threadGroup.setRampUp(this.getRampUp());
	                threadGroup.setSamplerController(loopController);
	                threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
	                threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

	                // Test Plan
	                TestPlan testPlan = new TestPlan("JMeter Script From AI");
	                
	                testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
	                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
	                testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

	                // Construct Test Plan from previously initialized elements
	                testPlanTree.add(testPlan);
	                HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
	                threadGroupHashTree.add(aiSampler);

	                // save generated test plan to JMeter's .jmx file format

					SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterHome.getPath() + slash + "scripts"+ slash + this.scenarioName));
					
	                //add Summarizer output to get test progress in stdout like:
	                // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
	                Summariser summer = null;
	                String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
	                if (summariserName.length() > 0) {
	                    summer = new Summariser(summariserName);
	                }

	                // Store execution results into a .jtl file, we can save file as csv also
	                String reportFile = jmeterHome.getPath() + slash + "report"+ slash +scenarioName+"_report.jtl";
	                String csvFile = jmeterHome.getPath() + slash + "report"+ slash +scenarioName+"_report.csv";
	                ResultCollector logger = new ResultCollector(summer);
	                logger.setFilename(reportFile);
	                ResultCollector csvlogger = new ResultCollector(summer);
	                csvlogger.setFilename(csvFile);
	                testPlanTree.add(testPlanTree.getArray()[0], logger);
	                testPlanTree.add(testPlanTree.getArray()[0], csvlogger);
	                // Run Test Plan
	                jmeter.configure(testPlanTree);
	                jmeter.run();
	                log.info("Test completed. See " + reportFile+ " file for results");
	                log.info("JMeter .jmx script is available at " + csvFile );
	                jmeter.reset();
	                jmeter.exit();
	                //System.exit(0);
	            }
	        }
	        else{

	        	log.error("jmeterHome property is not set or pointing to incorrect location");
	        System.exit(1);
	        }
	        } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
