package com.utd.aos.project.two;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class MutualExclusionTest {

	private Node node;
	private List<List<double[]>> allIntervals;
	
	public MutualExclusionTest(Node node) {
		setNode(node);
		setAllIntervals(new ArrayList<>());
	}
	
	public void sendCSIntervals(List<double[]> csIntervals) {
		Node coordinator = getNode().getNodeList()[0];
		if(coordinator.getID() == getNode().getID()) {
			getAllIntervals().add(csIntervals);
			return;
		}
		send(csIntervals, coordinator.getHostname(), coordinator.getPort());
	}
	
	public void receiveCSIntervals(List<double[]> csIntervals) {
		getAllIntervals().add(csIntervals);
		if(getAllIntervals().size() == getNode().getNumberOfNodes()) System.out.println("Does safety property hold: " +isAlgorithmSafe(getAllIntervals()));
	}
	
	private boolean isAlgorithmSafe(List<List<double[]>> allIntervals) {
		PriorityQueue<int[]> heap = new PriorityQueue<>(new Comparator<int[]>() {
			@Override
			public int compare(int[] n1, int[] n2) {
				double val1 = allIntervals.get(n1[0]).get(n1[1])[0];
				double val2 = allIntervals.get(n2[0]).get(n2[1])[0];
				if(val1 < val2) return -1;
				else if(val1 > val2) return 1;
				else return 0;
			}
		});
		
		for(int i=0;i<allIntervals.size();i++) heap.offer(new int[] {i, 0});
		int[] prev = heap.poll();
		if(allIntervals.get(prev[0]).size() >= 2) heap.offer(new int[] {prev[0], 1});
		
		while(!heap.isEmpty()) {
			int[] curr = heap.poll();
			if(allIntervals.get(prev[0]).get(prev[1])[1] > allIntervals.get(curr[0]).get(curr[1])[0]) 
				return false;
			if(curr[1]+1 < allIntervals.get(prev[0]).size()) heap.offer(new int[]{curr[0], curr[1]+1});
			prev = curr;
		} 
		return true;
	}

	private void send(List<double[]> csIntervals, String destHost, int destPort) {
        Socket s = null;
        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            //s = new Socket(InetAddress.getByName(destHost).getHostAddress(), destPort);
            s = new Socket("127.0.0.1", destPort);
            os = s.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(csIntervals);
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

	public List<List<double[]>> getAllIntervals() {
		return allIntervals;
	}

	public void setAllIntervals(List<List<double[]>> allIntervals) {
		this.allIntervals = allIntervals;
	}
}
