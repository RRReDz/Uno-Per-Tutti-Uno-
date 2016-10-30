/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.webclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.domain.Player;
import unoxtutti.webserver.WebRequest;

/**
 *
 * @author picardi
 */
public class WebClientConnection {

	protected final InetAddress ipAddress;
	protected final int port;


	public WebClientConnection(InetAddress ip, int port) {
		ipAddress = ip;
		this.port = port;
	}

	private Object sendRequest(WebRequest req) {
		Object ob = null;
		try {
			Socket socket = new Socket(getIpAddress(), getPort());
			ObjectOutputStream sockOut = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream sockIn = new ObjectInputStream(socket.getInputStream());
			try {
				sockOut.writeObject(req);
				ob = sockIn.readObject();
			} catch (IOException | ClassNotFoundException ex) {
				Logger.getLogger(WebClientConnection.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				sockIn.close();
				sockOut.close();
				socket.close();
			}

		} catch (IOException ex) {
			Logger.getLogger(WebClientConnection.class.getName()).log(Level.SEVERE, null, ex);
		}
		return ob;
	}

	public boolean createUser(String userName, String email, String password) {
		WebRequest req = new WebRequest("createUser");
		Object[] pars = new Object[3];
		pars[0] = userName;
		pars[1] = email;
		pars[2] = password;
		req.setParameters(pars);
		Boolean bo = (Boolean) sendRequest(req);
		return bo;
	}

	public Player verify(String email, String password) {
		WebRequest req = new WebRequest("verify");
		Object[] pars = new Object[2];
		pars[0] = email;
		pars[1] = password;
		req.setParameters(pars);
		Player pl = (Player) sendRequest(req);
		return pl;
	}

	/**
	 * @return the ipAddress
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
}
