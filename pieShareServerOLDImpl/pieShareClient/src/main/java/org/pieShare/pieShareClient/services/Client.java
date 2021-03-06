/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareClient.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.pieShare.pieShareClient.api.Callback;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Richard
 */
public class Client {

	private DatagramSocket socket;
	private final ExecutorService executor;
	private String name = null;
	private ClientSendTask sendTask;
	private ClientTask task;
	private int localPort = 4321;

	public Client() {
		task = new ClientTask();
		sendTask = new ClientSendTask();
		task.setSendTask(sendTask);

		executor = Executors.newCachedThreadPool();
	}

	public void connect(String from, String to, int port) {

		if (port != -1) {
			this.localPort = port;
		}

		try {
			socket = new DatagramSocket(localPort);
		}
		catch (SocketException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}

		this.name = from;
		String serverAddress = "server.piesystems.org";//"128.130.172.205";//"richy.ddns.net";//"192.168.0.22";
		int serverPort = 6312;
		String registerMsg = "{\"type\":\"register\", \"name\":\"%s\", \"localAddress\":\"%s\", \"localPort\":%s, \"privateAddress\":\"%s\", \"privatePort\":%s}";
		String connectMsg = "{\"type\":\"connect\", \"from\":\"%s\", \"to\":\"%s\"}";
		String ackMsg = "{\"type\":\"ACK\", \"from\":\"%s\"}";
		String registerAndConnectMag = "{\"type\":\"registerAndConnect\", \"regMsg\":%s, \"connectMsg\":%s}";
		task.setSocket(socket);

		sendTask.setSocket(socket);
		sendTask.setName(name);

		task.setCallback(new Callback() {

			public void Handle(JsonObject client) {
				sendTask.setConnectionData(client);
				sendTask.setHost(client.getString("privateAddress"));
				sendTask.setPort(client.getInt("privatePort"));
				executor.execute(sendTask);
			}
		});

		executor.execute(task);

		try {
			InetAddress IP = InetAddress.getLocalHost();
			String localIP = IP.getHostAddress();

			String text = String.format(registerMsg, from, localIP, localPort, localIP, localPort);
			DatagramPacket packet = new DatagramPacket(text.getBytes(), text.length(), InetAddress.getByName(serverAddress), serverPort);

			socket.send(packet);

			//Remove this by insert new message for registering and ocnnecting at same time
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException ex) {
				Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
			}

			if (to != null) {
				text = String.format(connectMsg, from, to);
				packet = new DatagramPacket(text.getBytes(), text.length(), InetAddress.getByName(serverAddress), serverPort);
				socket.send(packet);
			}

		}
		catch (UnknownHostException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public JsonObject processInput(String input) {
		ByteArrayInputStream byteInStream = new ByteArrayInputStream(input.getBytes());
		JsonReader jsonReader = Json.createReader(byteInStream);
		JsonObject ob = jsonReader.readObject();
		PieLogger.info(this.getClass(), String.format("ConnectionText: %s", ob.toString()));
		return ob;
	}
}
