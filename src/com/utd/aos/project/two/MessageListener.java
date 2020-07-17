/**
 * 
 */
package com.utd.aos.project.two;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author indhiramudrageda
 *
 */
public class MessageListener extends Thread{ 
	private InputStream inStream;
    private ObjectInputStream objectInputStream; 
    private ServerSocket tcpServersocket; 
    private Socket socket;
    private final MutualExclusionService service;
    
	public MessageListener(MutualExclusionService service) {
		this.service = service;
		try {
			tcpServersocket = new ServerSocket(service.getNode().getPort());
		} catch (IOException e) {
			System.out.println("Error creating server socket: "+ e.getMessage());
		}
	}
	
	@Override
    public void run()  
    { 
        while (true)  
        { 
            try { 
            	// receive messages from other processes 
            	socket = tcpServersocket.accept();
            	inStream = socket.getInputStream();
        		objectInputStream = new ObjectInputStream(inStream); 
                Object obj = objectInputStream.readObject();
            } 
            catch (IOException | ClassNotFoundException e) { 
            	System.out.println("Error reading message sent to :" + service.getNode().getID()+e.getMessage()); 
            	System.out.println("Error reading message sent to :" + service.getNode().getID()+e.getStackTrace()); 
            	try {
					inStream.close();
					objectInputStream.close();
					socket.close();
				} catch (IOException e1) {
					System.out.println("Error closing connections: "+ e1.getMessage());
				}
            } 
        }           
    }
}
