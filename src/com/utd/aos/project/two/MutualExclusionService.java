package com.utd.aos.project.two;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class MutualExclusionService {
    private int clock; //unsafe
    private volatile boolean isCSEligible; //unsafe
    private PriorityQueue<Message> queue; //unsafe
    private Node node;
    private Message currentRequest; //unsafe
    private Set<Integer> receivedMessages; 
    public int sentMsgs;
    public int rcvdMsgs;

    public MutualExclusionService(Node node) {
        setNode(node);
        setReceivedMessages(new HashSet<>());
        this.queue = new PriorityQueue<>(new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return m1.getClock() - m2.getClock() == 0 ? m1.getID() - m2.getID() : m1.getClock() - m2.getClock();
            }
        });
    }

    public boolean csEnter() {
    	synchronized (this) {
    		//1. place current request message in queue.
            incremeantClock();
            setCurrentRequest(new Message(getNode().getID(), getClock(), Message.TYPE_REQUEST));
            getQueue().offer(getCurrentRequest());
		}

        //2. broadcast request messages to all other nodes.
        sendRequestMessage();

        while (!isCSEligible());
        incremeantClock();
        return true;
    }

    public synchronized void csLeave() {
        //1. remove the current request message from queue.
        getQueue().remove(getCurrentRequest());

        //2. broadcast release message to all other nodes
        incremeantClock();
        Message message = new Message(getNode().getID(), getClock(), Message.TYPE_RELEASE);
        sendReleaseMessage(message);
        setCurrentRequest(null);
        getReceivedMessages().clear();
        setCSEligible(false);
    }

    private synchronized void sendRequestMessage() {
        //1. broadcast request messages to all other nodes.
        for (Node node : getNode().getNodeList()) {
            if (node.getID() == getNode().getID()) continue;
            send(getCurrentRequest(), node.getHostname(), node.getPort());
        }
    }

    public synchronized void receiveRequestMessage(Message message) {
        //1. add request message to queue.
        getQueue().offer(message);

        //2. Update clock
        updateClock(message.getClock());
        
        //3. send reply message back to the requesting process
        incremeantClock();
        Node sender = getNode().getNodeList()[message.getID()];
        sendReplyMessage(new Message(getNode().getID(), getClock(), Message.TYPE_REPLY), sender.getHostname(), sender.getPort());

        //4. check for cs eligibility
        checkCSEligibility(message);
    }

	private void sendReplyMessage(Message message, String destHost, int destPort) {
        //1. broadcast reply messages to all other nodes.
        send(message, destHost, destPort);
    }

    public synchronized void receiveReplyMessage(Message message) {
        //1. Increment clock
    	updateClock(message.getClock());
        //2. check for cs eligibility
        checkCSEligibility(message);
    }

    private void sendReleaseMessage(Message message) {
        //1. broadcast release messages to all other nodes.
    	for (Node node : getNode().getNodeList()) {
            if (node.getID() == getNode().getID()) continue;
            send(message, node.getHostname(), node.getPort());
        }
    }

    public synchronized void receiveReleaseMessage(Message message) {
        //1. Increment clock;
    	updateClock(message.getClock());
        //2. remove the request message of the process which sent this message from queue.
        getQueue().remove(message);
        //3. check for cs eligibility
        checkCSEligibility(message);
    }

    private synchronized void checkCSEligibility(Message message) {
    	if(getCurrentRequest() == null || isCSEligible()) return;
    	
        //1. If received message has timestamp larger than that of its own request, add to receivedMessages list
        if (message.getClock() > getCurrentRequest().getClock()) 
        	getReceivedMessages().add(message.getID());

        //2. check if list size is equal to n-1 and that the top of queue is current request. If so, set csEligible to true.
        if (getReceivedMessages().size() == getNode().getNumberOfNodes() - 1 && getCurrentRequest().equals(getQueue().peek())) 
        	setCSEligible(true);
    }

    private void send(Message message, String destHost, int destPort) {
    	sentMsgs++;
        Socket s = null;
        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            s = new Socket(InetAddress.getByName(destHost).getHostAddress(), destPort);
           // s = new Socket("127.0.0.1", destPort);
            os = s.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(message);
            oos.close();
            os.close();
            s.close();
        } catch (IOException e) {
            System.out.println("Error sending data to " + destHost + ": " + e.getMessage());
        }
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Message getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(Message currentRequest) {
        this.currentRequest = currentRequest;
    }

    public Set<Integer> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(Set<Integer> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public void incremeantClock() {
        this.clock++;
    }
    
    private void updateClock(int clock) {
		setClock(Math.max(getClock(), clock)+1);
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
