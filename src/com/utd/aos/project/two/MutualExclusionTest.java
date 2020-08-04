package com.utd.aos.project.two;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class MutualExclusionTest {

	private Node node;
	private List<List<long[]>> allIntervals;
	
	public MutualExclusionTest(Node node) {
		setNode(node);
		setAllIntervals(new ArrayList<>());
	}
	
	public void sendCSIntervals(List<long[]> csIntervals) {
		Node coordinator = getNode().getNodeList()[0];
		if(coordinator.getID() == getNode().getID()) {
			getAllIntervals().add(csIntervals);
			return;
		}
		Node.send(csIntervals, coordinator.getHostname(), coordinator.getPort());
	}
	
	public void receiveCSIntervals(List<long[]> csIntervals) {
		getAllIntervals().add(csIntervals);
		if(getAllIntervals().size() == getNode().getNumberOfNodes()) System.out.println("Does safety property hold: " +isAlgorithmSafe(getAllIntervals()));
	}
	
	public void receiveTimestampMessage(Message msg) {
		Node sender = getNode().getNodeList()[msg.getID()];
		Node.send(new Message(getNode().getID(), System.currentTimeMillis(), Message.TYPE_TIMESTAMP_RESPONSE), sender.getHostname(), sender.getPort());
	}
	
	private boolean isAlgorithmSafe(List<List<long[]>> allIntervals) {
		PriorityQueue<int[]> heap = new PriorityQueue<>(new Comparator<int[]>() {
			@Override
			public int compare(int[] n1, int[] n2) {
				long val1 = allIntervals.get(n1[0]).get(n1[1])[0];
				long val2 = allIntervals.get(n2[0]).get(n2[1])[0];
				if(val1 < val2) return -1;
				else if(val1 > val2) return 1;
				else return 0;
			}
		});
		
		for(int i=0;i<allIntervals.size();i++) heap.offer(new int[] {i, 0});
		int[] prev = heap.poll();
		if(allIntervals.get(prev[0]).size() >= 2) heap.offer(new int[] {prev[0], 1});
		int[] first = heap.peek();
		long end = allIntervals.get(first[0]).get(first[1])[0]+10000;
		int count = 0;
		while(!heap.isEmpty()) {
			int[] curr = heap.poll();
			if(allIntervals.get(curr[0]).get(curr[1])[1] <= end) count++;
			if(allIntervals.get(prev[0]).get(prev[1])[1] > allIntervals.get(curr[0]).get(curr[1])[0]) {
				System.out.println("prev: "+allIntervals.get(prev[0]).get(prev[1])[1]+" curr: "+allIntervals.get(curr[0]).get(curr[1])[0]);
				System.out.println("prev: "+prev[0]+" curr: "+curr[0]);
				return false;
			}
			if(curr[1]+1 < allIntervals.get(prev[0]).size()) heap.offer(new int[]{curr[0], curr[1]+1});
			prev = curr;
		} 
		System.out.println("System throughput (requests completed/sec): "+ count/10);
		return true;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public List<List<long[]>> getAllIntervals() {
		return allIntervals;
	}

	public void setAllIntervals(List<List<long[]>> allIntervals) {
		this.allIntervals = allIntervals;
	}
}
