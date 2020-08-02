/**
 * 
 */
package com.utd.aos.project.two;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Node {

	private int ID;
	private String hostname;
	private int port;
	private Node[] nodeList;
	
	private int numberOfNodes;
	private int interRequestDelay;
	private int csExecutionTime;
	private int requestsToGenerate;
	private List<long[]> csIntervals;
	
	private MutualExclusionService mutexService;
	private MutualExclusionTest mutexTest;
	
	private static final String CONFIG_FILE = "config.txt";
	public static final int COORDINATOR = 0;
	
	public Node(int ID, String hostname, int port) {
		setID(ID);
		setHostname(hostname);
		setPort(port);
	}
	
	public Node(int ID, String hostname, int port, int numberOfNodes, int interRequestDelay, int csExecutionTime, int requestsToGenerate, Node[] nodeList) {
		setID(ID);
		setHostname(hostname);
		setPort(port);
		setNumberOfNodes(numberOfNodes);
		setInterRequestDelay(interRequestDelay);
		setCsExecutionTime(csExecutionTime);
		setRequestsToGenerate(requestsToGenerate);
		setNodeList(nodeList);
		setCsIntervals(new ArrayList<>());
		
		setMutexService(new MutualExclusionService(this));
		setMutexTest(new MutualExclusionTest(this));
		
		//Generate critical section requests periodically
		CSRequestGenerator requestGenerator = new CSRequestGenerator(this);
		requestGenerator.start();
		
		//Listen to messages coming from other processes.
        MessageListener messageListener = new MessageListener(this);
        messageListener.start();
	}
	
	public static void main(String[] args) {
		int numberOfNodes = 0;
		int interRequestDelay = 0;
		int csExecutionTime = 0;
		int requestsToGenerate = 0;
		int ID = 0;
		
		//String ip = args[0];
		try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
			String currentLine;
			int n = 0;
			InetAddress inetAddress = InetAddress.getLocalHost();
			while ((currentLine = br.readLine()) != null) {
				currentLine = preprocessConfig(currentLine);
				if(currentLine.length() == 0) continue; //invalid line
				String[] params = currentLine.split("\\s+");
				numberOfNodes = Integer.parseInt(params[0]);
				interRequestDelay = Integer.parseInt(params[1]);
				csExecutionTime = Integer.parseInt(params[2]);
				requestsToGenerate = Integer.parseInt(params[3]);
				n = numberOfNodes;
				break;
			}
			Node[] nodeList = new Node[numberOfNodes];
			while(n > 0) {
				currentLine = preprocessConfig(br.readLine());
				if(currentLine.length() == 0) continue; //invalid line
				String[] params = currentLine.split("\\s+");
				int currID = Integer.parseInt(params[0]);
				String currHost = params[1]+".utdallas.edu";
				int currPort = Integer.parseInt(params[2]);
				
				//if(ip.equals(InetAddress.getByName(currHost).getHostAddress())) ID = currID;
				if(inetAddress.getHostAddress().equals(InetAddress.getByName(currHost).getHostAddress())) ID = currID;
				nodeList[currID] = new Node(currID, currHost, currPort);
				n--;
			}
			
			System.out.println("Starting node:" + ID);
			new Node(ID, nodeList[ID].getHostname(), nodeList[ID].getPort(), numberOfNodes, interRequestDelay, csExecutionTime, requestsToGenerate, nodeList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getID() {
		return ID;
	}

	private void setID(int ID) {
		this.ID = ID;
	}

	public String getHostname() {
		return hostname;
	}

	private void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

	public Node[] getNodeList() {
		return nodeList;
	}

	public void setNodeList(Node[] nodeList) {
		this.nodeList = nodeList;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public int getInterRequestDelay() {
		return interRequestDelay;
	}

	public void setInterRequestDelay(int interRequestDelay) {
		this.interRequestDelay = interRequestDelay;
	}

	public int getCsExecutionTime() {
		return csExecutionTime;
	}

	public void setCsExecutionTime(int csExecutionTime) {
		this.csExecutionTime = csExecutionTime;
	}

	public int getRequestsToGenerate() {
		return requestsToGenerate;
	}

	public void setRequestsToGenerate(int requestsToGenerate) {
		this.requestsToGenerate = requestsToGenerate;
	}
	
	public void decrementRequestsToGenerate() {
		this.requestsToGenerate--;
	}

	public List<long[]> getCsIntervals() {
		return csIntervals;
	}

	public void setCsIntervals(List<long[]> csIntervals) {
		this.csIntervals = csIntervals;
	}

	public MutualExclusionService getMutexService() {
		return mutexService;
	}

	public void setMutexService(MutualExclusionService mutexService) {
		this.mutexService = mutexService;
	}

	public MutualExclusionTest getMutexTest() {
		return mutexTest;
	}

	public void setMutexTest(MutualExclusionTest mutexTest) {
		this.mutexTest = mutexTest;
	}

	private static String preprocessConfig(String currentLine) {
		currentLine = currentLine.trim();
		if(currentLine.length() == 0 || !Character.isDigit(currentLine.charAt(0))) return "";
		int comment = currentLine.indexOf('#');
		currentLine = comment == -1 ? currentLine : currentLine.substring(0, comment) ;
		return currentLine;
	}
	
	public static int getRandomIntegerBetweenRange(double min, double max){
	    double random = (int)(Math.random()*((max-min)+1))+min;
	    return (int) random;
	}
}
