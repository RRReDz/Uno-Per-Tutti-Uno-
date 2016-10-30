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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author picardi
 */
public class WebServerPrevious implements Runnable {

	private boolean stopSuggested = false;
	private boolean stopped = true;

	public synchronized boolean isStopSuggested() {
		return stopSuggested;
	}

	public synchronized boolean isStopped() {
		return stopped;
	}

	protected synchronized void setStopped(boolean s) {
		stopped = s;
	}

	private class WebServerHelper implements Runnable {

		private final Socket clientSocket;
		ObjectInputStream sockIn;
		ObjectOutputStream sockOut;
		private boolean stopSuggested = false;
		private boolean stopped = true;

		private WebServerHelper(Socket s) {
			clientSocket = s;
		}

		synchronized public void setStopSuggested(boolean b) {
			this.setStopSuggested(b);
			if (b) {
				try {
					clientSocket.close();
				} catch (IOException ex) {
					Logger.getLogger(WebServerPrevious.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		@Override
		public void run() {
			try {
				try {
					sockIn = new ObjectInputStream(clientSocket.getInputStream());
					sockOut = new ObjectOutputStream(clientSocket.getOutputStream());
					WebRequest req;
					setStopped(false);
					while (!isStopSuggested() && (req = (WebRequest) sockIn.readObject()) != null) {
						if (req.isDummyRequest()) {
							setStopSuggested(true);
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
					}
					sockOut.close();
					sockIn.close();
				} finally {
					setStopped(true);
					try {
						clientSocket.close();
					} catch (IOException ex) {
						Logger.getLogger(WebServerPrevious.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} catch (IOException | ClassNotFoundException ex) {
				if (!isStopSuggested()) {
					Logger.getLogger(WebServerPrevious.class.getName()).log(Level.SEVERE, null, ex);
				} else {
					System.out.println("Helper correctly stopped.");
				}
			}
		}

		public synchronized boolean isStopSuggested() {
			return stopSuggested;
		}

		public synchronized boolean isStopped() {
			return stopped;
		}

		protected synchronized void setStopped(boolean s) {
			stopped = s;
		}

	}

	public static final int PORT = 9000;
	private final ArrayList<WebRequestHandler> requestHandlers;
	private static InetAddress ipAddress;
	private ServerSocket ssocket;
	private static DBController dbController;
	private final ArrayList<WebServerHelper> helpers;

	public static DBController getDBController() {
		return dbController;
	}

	public void registerRequestHandler(WebRequestHandler webRequestHandler) {
		this.requestHandlers.add(webRequestHandler);
	}

	public WebServerPrevious(DBController dbc) {
		requestHandlers = new ArrayList<>();
		helpers = new ArrayList<>();
		dbController = dbc;
	}

	public synchronized InetAddress getInetAddress() {
		return ipAddress;
	}

	synchronized public void setStopSuggested(boolean b) {
		this.setStopSuggested(b);
		if (isStopped()) {
			return;
		}
		if (b) {
			try {
				Socket socket = new Socket(ipAddress, PORT);
				ObjectOutputStream sockOut = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream sockIn = new ObjectInputStream(socket.getInputStream());
				sockIn.close();
				sockOut.close();
				socket.close();
			} catch (IOException ex) {
				Logger.getLogger(WebServerPrevious.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void run() {
		try {
			ssocket = new ServerSocket(PORT);
			ipAddress = ssocket.getInetAddress();
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
				t.isStopSuggested();
				System.out.println("Suggesting stop to helper");
			}
			boolean canClose = false;
			while (!canClose) {
				canClose = true;
				for (WebServerHelper t : helpers) {
					if (!t.isStopped()) {
						canClose = false;
					}
				}
			}
			System.out.println("All helpers stopped");
			setStopped(true);
			ssocket.close();
		} catch (IOException ex) {
			Logger.getLogger(WebServerPrevious.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
