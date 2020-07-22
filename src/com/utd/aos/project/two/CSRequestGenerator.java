package com.utd.aos.project.two;

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
		while(node.getRequestsToGenerate() > 0) {
			try {
				getNode().getMutexService().csEnter();
				double start = System.currentTimeMillis();
				//System.out.println("Enterring CS :"+start);
				Thread.sleep(node.getCsExecutionTime());
				getNode().getMutexService().csLeave();
				double end = System.currentTimeMillis();
				//System.out.println("Leaving CS :"+end);
				node.getCsIntervals().add(new double[] {start, end});
				node.decrementRequestsToGenerate();
				Thread.sleep(node.getInterRequestDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finisehd all requests: "+getNode().getMutexService().getClock());
		getNode().getMutexTest().sendCSIntervals(node.getCsIntervals());
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

}
