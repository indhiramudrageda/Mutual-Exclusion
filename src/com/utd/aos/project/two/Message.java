/**
 * 
 */
package com.utd.aos.project.two;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	private int ID;
	private int clock;
	private String type;
	public static final String TYPE_REQUEST = "REQUEST";
	public static final String TYPE_REPLY = "REPLY";
	public static final String TYPE_RELEASE = "RELEASE";
	
	public Message(int ID, int clock, String type) {
		this.ID = ID;
		this.clock = clock;
		this.type = type;
	}

	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}
	
	public int getClock() {
		return clock;
	}

	public void setClock(int clock) {
		this.clock = clock;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object o) {
		Message m = (Message)o;
		return this.ID == m.ID;
	}
}
