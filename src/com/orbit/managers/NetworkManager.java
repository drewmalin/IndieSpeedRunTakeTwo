package com.orbit.managers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.orbit.core.Game;
import com.orbit.core.Packet;
import com.orbit.core.WebService;

public class NetworkManager extends Thread {
	
	private Game gameHandle;
	private Packet p;
	private String host;
	private int port;
	
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	public	WebService put;
	public WebService get;
	
	private Socket socket;
	
	public final int ADD_ENTITY 	= 0;
	public final int UPDATE_ENTITY  = 1;
	public final int DROP_ENTITY 	= 2;
	
	public NetworkManager(Game g) {
		gameHandle = g;
	}
	
	public void setServerURL(String h, int p) {
		host = h;
		port = p;
	}

	public void connect() {
		try {
			SocketAddress sockaddr = new InetSocketAddress(host, port);
			socket = new Socket();	
			try {
				socket.connect(sockaddr, 5000);
			} catch (ConnectException e) {
				System.out.println("Error connecting to server: ");
				e.printStackTrace();
			}
			
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			this.start();
			
		} catch (IOException e) {
			System.out.println("Socket error: ");
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			if (oos != null) oos.close();
			if (ois != null) ois.close();
			if (socket != null) socket.close();
		} catch (IOException e) {
			System.out.println("Error disconnecting: ");
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while (true) {
				p = (Packet) ois.readObject();
				
				get.work(p);	
			}
		} catch (Exception e) {
			System.out.println("Network error, attempting to reconnect: ");
			connect();
		}
	}

	public void send(Packet p) {
		try {
			oos.writeObject(p);
			oos.flush();
		} catch (Exception e) {
			System.out.println("ERROR: Exception while sending packet.");
			e.printStackTrace();
		}
	}
	
	public void setPutService(WebService putWebService) {
		put = putWebService;
	}

	public void setGetService(WebService getWebService) {
		get = getWebService;
	}

}
