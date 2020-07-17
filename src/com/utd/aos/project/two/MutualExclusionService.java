package com.utd.aos.project.two;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MutualExclusionService {
	private int clock;
	private volatile boolean isCSEligible;
	private PriorityQueue<Message> queue;
	private Node node;
	
	public MutualExclusionService(Node node) {
		setNode(node);
		this.queue = new PriorityQueue<>(new Comparator<Message>() {
			@Override
			public int compare(Message m1, Message m2) {
				return m1.getClock()-m2.getClock() == 0 ? m1.getID()-m2.getID() : m1.getClock()-m2.getClock();
			}
		});
		
		//Listen to messages coming from other processes.
		MessageListener messageListener = new MessageListener(this);
		messageListener.start(); 
	}
	
	public boolean csEnter() {
		//1. place current request message in queue.
		//2. broadcast request messages to all other nodes.
		while(!isCSEligible()) {
			
		}
		return true;
	}
	
	public void csLeave() {
		//1. remove the current request message from queue.
		//2. broadcast release message to all other nodes
	}
	
	public void sendRequestMessage() {
		//1. broadcast request messages to all other nodes.
	}
	
	public void receiveRequestMessage(Message message) {
		//1. add request message to queue.
		//2. send reply message back to the requesting process
		//3. check for cs eligibility
	}
	
	public void sendReplyMessage() {
		//1. broadcast reply messages to all other nodes.
	}
	
	public void receiveReplyMessage() {
		//1. check for cs eligibility
	}
	
	public void sendReleaseMessage() {
		//1. broadcast release messages to all other nodes.
	}
	
	public void receiveReleaseMessage() {
		//1. remove the request message of the process which sent this message from queue.
		//2. check for cs eligibility
	}
	
	private void send(Message message, int destID, String destHost, int destPort) {
		Socket s = null;
		OutputStream os = null;
		ObjectOutputStream oos = null;
		try {
			s = new Socket(InetAddress.getByName(destHost).getHostAddress(), destPort);
			//s = new Socket("127.0.0.1", destPort);
			os = s.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(message);
		} catch (IOException e) {
			System.out.println("Error sending data to "+destHost+": "+ e.getMessage());
		} 
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int getClock() {
		return clock;
	}
	public void setClock(int clock) {
		this.clock = clock;
	}
	public boolean isCSEligible() {
		return isCSEligible;
	}
	public void setCSEligible(boolean isCSEligible) {
		this.isCSEligible = isCSEligible;
	}
	public PriorityQueue<Message> getQueue() {
		return queue;
	}
	public void setQueue(PriorityQueue<Message> queue) {
		this.queue = queue;
	}
}
