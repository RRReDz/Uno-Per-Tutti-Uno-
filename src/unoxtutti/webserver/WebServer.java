/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.webserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author picardi
 */
public class WebServer implements Runnable {
	
	// CHANGE THIS TO RUN ON YOUR MACHINE
	public final static int WEBSERVER_PORT = 9000;
	public final static String WEBSERVER_IP = "localhost";

	private class WebServerHelper implements Runnable {

		private final Socket clientSocket;
		private ObjectInputStream sockIn;
		private ObjectOutputStream sockOut;
		private boolean isStopping;

		private WebServerHelper(Socket s) {
			clientSocket = s;
			isStopping = false;
		}

		synchronized private void stop() {
			boolean wasStopping = isStopping;
			isStopping = true;
			if (!wasStopping) {
				close();
			}
		}

		synchronized private void close() {
			try {
				sockOut.close();
				sockIn.close();
				clientSocket.close();
			} catch (IOException ex) {
				Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		@Override
		public void run() {
			try {
				System.out.println("Running helper thread");
				sockIn = new ObjectInputStream(clientSocket.getInputStream());
				sockOut = new ObjectOutputStream(clientSocket.getOutputStream());
				WebRequest req;
				req = (WebRequest) sockIn.readObject();
				if (req.isDummyRequest()) {
					sockOut.writeObject("ok");
				} else {
					WebRequestHandler theHandler = null;
					boolean conflict = false;
					for (WebRequestHandler wrh : requestHandlers) {
						if (wrh.canHandle(req)) {
							if (theHandler == null) {
								theHandler = wrh;
							} else {
								conflict = true;
							}
						}
					}

					if (theHandler == null) {
						throw new WebRequestException("No handler for Request " + req.toString());
					} else if (conflict) {
						throw new WebRequestException("Multiple handlers for Request " + req.toString());
					} else {
						theHandler.handle(req, sockOut);
					}
				}

				stop();
				System.out.println("Helper thread terminated");

			} catch (IOException | ClassNotFoundException ex) {
				if (isStopping) {
					System.out.println("legitimate close");
				} else {
					Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private final ArrayList<WebRequestHandler> requestHandlers;
	private ServerSocket ssocket;
	private static DBController dbController;
	private final ArrayList<WebServerHelper> helpers;

	public static DBController getDBController() {
		return dbController;
	}

	public void registerRequestHandler(WebRequestHandler webRequestHandler) {
		this.requestHandlers.add(webRequestHandler);
	}

	public WebServer(DBController dbc) {
		requestHandlers = new ArrayList<>();
		helpers = new ArrayList<>();
		dbController = dbc;
	}

	public synchronized InetAddress getInetAddress() {
		try {
			return InetAddress.getByName(WEBSERVER_IP);
		} catch (UnknownHostException ex) {
			Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	synchronized public void setStopSuggested(boolean b) {
			stopSuggested = b;
		if (isStopped()) {
			return;
		}
		if (b) {
			try {
				Socket socket = new Socket(InetAddress.getByName(WEBSERVER_IP), WEBSERVER_PORT);
				ObjectOutputStream sockOut = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream sockIn = new ObjectInputStream(socket.getInputStream());
				sockOut.writeObject(WebRequest.getDummyRequest());
				sockIn.readObject();
				sockIn.close();
				sockOut.close();
				socket.close();
			} catch (IOException | ClassNotFoundException ex) {
				Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void run() {
		try {
			ssocket = new ServerSocket(WEBSERVER_PORT);
			setStopSuggested(false);
			setStopped(false);
			while (!isStopSuggested()) {
				Socket clientSocket = ssocket.accept();
				System.out.println("Connection accepted.");
				WebServerHelper h = new WebServerHelper(clientSocket);
				Thread t = new Thread(h);
				helpers.add(h);
				t.start();
			}
			System.out.println("Server out of main cycle");
			for (WebServerHelper t : helpers) {
				t.stop();
			}
			System.out.println("All helpers stopped");
			ssocket.close();
			setStopped(true);
		} catch (IOException ex) {
			Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) {
		try {
			DBController dbc = new DBController();
			dbc.connect();
			WebServer app = new WebServer(dbc);

			/* Aggiungere qui altri request handler */
			app.registerRequestHandler(new AuthRequestHandler());

			(new Thread(app)).start();

			WebServerStopDialog dialog = new WebServerStopDialog(null, app);
			dialog.setVisible(true);
			dbc.disconnect();
		} catch (SQLException ex) {
			Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
		private boolean stopSuggested = false;
		private boolean stopped = true;
		
		
		public synchronized boolean isStopSuggested() {
			return stopSuggested;
		}
		
		public synchronized boolean isStopped() {
			return stopped;
		}
		
		synchronized protected void setStopped(boolean s) {
			stopped = s;
		}	
	
}
