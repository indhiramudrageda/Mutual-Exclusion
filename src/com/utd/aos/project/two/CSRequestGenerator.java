package com.utd.aos.project.two;

import java.util.Random;

public class CSRequestGenerator extends Thread {

	private Node node;
	
	public CSRequestGenerator(Node node) {
		this.node = node;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(10000); //inital delay
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Random rand = new Random();
		while(node.getRequestsToGenerate() > 0) {
			try {
				getNode().getMutexService().csEnter();
				long start = System.currentTimeMillis();
				Thread.sleep((long)rand.nextGaussian()+node.getCsExecutionTime());
				long end = System.currentTimeMillis();
				getNode().getMutexService().csLeave();
				node.getCsIntervals().add(new long[] {start, end});
				node.decrementRequestsToGenerate();
				Thread.sleep((long)rand.nextGaussian()+node.getInterRequestDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (node.getMutexService()) {
			System.out.println("Finisehd all requests: "+getNode().getMutexService().getClock());
			System.out.println("Msgs sent: "+getNode().getMutexService().sentMsgs+" Rcvd Msgs: "+getNode().getMutexService().rcvdMsgs);
		}
		
		getNode().getMutexTest().sendCSIntervals(node.getCsIntervals());
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

}
