package main;

import java.io.*;
import java.net.*;
import java.util.*;

public class MusicServer {

    private ArrayList<ObjectOutputStream> clientOutputStreams;
    private int portNum = 5000;

    public static void main(String[] args) {
	new MusicServer().go();

    }

    public class ClientHandler implements Runnable {
	private ObjectInputStream in;
	private Socket clientSocket;

	public ClientHandler(Socket sock) {
	    try {
		clientSocket = sock;
		in = new ObjectInputStream(clientSocket.getInputStream());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	@Override
	public void run() {
	    Object o2 = null;
	    Object o1 = null;
	    try {
		while ((o1 = in.readObject()) != null) {
		    o2 = in.readObject();

		    System.out.println("read two objects");
		    tellEveryone(o1, o2);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public void go() {
	clientOutputStreams = new ArrayList<ObjectOutputStream>();
	try {
	    ServerSocket serverSock = new ServerSocket(portNum);
	    while (true) {
		Socket clientSocket = serverSock.accept();
		ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
		clientOutputStreams.add(out);

		Thread t = new Thread(new ClientHandler(clientSocket));
		t.start();

		System.out.println("got a connection");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void tellEveryone(Object one, Object two) {
	Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
	while (it.hasNext()) {
	    try {
		ObjectOutputStream out = it.next();
		out.writeObject(one);
		out.writeObject(two);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

}
