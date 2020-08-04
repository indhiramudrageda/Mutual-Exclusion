package com.utd.aos.project.two;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
public class CSRequestGenerator extends Thread {

	private Node node;
	private volatile boolean receivedTimestamp;
	private long rcvdTimestamp;
	private long t1;
	
	public CSRequestGenerator(Node node) {
		this.node = node;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(10000); //initial delay
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Random rand = new Random();
		while(node.getRequestsToGenerate() > 0) {
			try {
				long reqRaised = System.currentTimeMillis();
				getNode().getMutexService().csEnter();
				
				long start = System.currentTimeMillis();
				Node coordinator = getNode().getNodeList()[0];
				if(coordinator.getID() != getNode().getID()) {
					long t0 = requestTimestamp();
					while(!isReceivedTimestamp());
					start = getRcvdTimestamp()+(getT1()-t0)/2;
					setReceivedTimestamp(false);
				}
			    //System.out.println("Enterring cs at node: "+ getNode().getID()+" at time: "+start);

				Thread.sleep((long)rand.nextGaussian()+node.getCsExecutionTime());
				
				long end = System.currentTimeMillis();
				if(coordinator.getID() != getNode().getID()) {
					long t0 = requestTimestamp();
					while(!isReceivedTimestamp());
					end = getRcvdTimestamp()+(getT1()-t0)/2;
					setReceivedTimestamp(false);
				}
				//System.out.println("Leaving cs at node: "+ getNode().getID()+" at time: "+end);
				
				getNode().getMutexService().csLeave();
				
				long response = end-reqRaised;
				System.out.println("Response time (in millis) at node: "+ getNode().getID() +" "+ response);
				
				node.getCsIntervals().add(new long[] {start, end});
				node.decrementRequestsToGenerate();
				Thread.sleep((long)rand.nextGaussian()+node.getInterRequestDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		getNode().getMutexTest().sendCSIntervals(node.getCsIntervals());
	}

	private long requestTimestamp() {
		Node coordinator = getNode().getNodeList()[0];
		Socket s = null;
        OutputStream os = null;
        ObjectOutputStream oos = null;
        long sendTime = 0;
        try {
            s = new Socket(InetAddress.getByName(coordinator.getHostname()).getHostAddress(), coordinator.getPort());
            //s = new Socket("127.0.0.1", destPort);
            os = s.getOutputStream();
            oos = new ObjectOutputStream(os);
            sendTime = System.currentTimeMillis();
            oos.writeObject(new Message(getNode().getID(), 0L, Message.TYPE_TIMESTAMP_REQUEST));
            oos.close();
            os.close();
            s.close();
        } catch (IOException e) {
            System.out.println("Error sending data to " + coordinator.getHostname() + ": " + e.getMessage());
        }
		return sendTime;
	}
	
	public void receiveTimestampMessage(Message msg, long rcvdTime) {
		setT1(rcvdTime);
		setRcvdTimestamp(msg.getTimestamp());
		setReceivedTimestamp(true);
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public boolean isReceivedTimestamp() {
		return receivedTimestamp;
	}

	public void setReceivedTimestamp(boolean receivedTimestamp) {
		this.receivedTimestamp = receivedTimestamp;
	}

	public long getRcvdTimestamp() {
		return rcvdTimestamp;
	}

	public void setRcvdTimestamp(long rcvdTimestamp) {
		this.rcvdTimestamp = rcvdTimestamp;
	}

	public long getT1() {
		return t1;
	}

	public void setT1(long t1) {
		this.t1 = t1;
	}

}
