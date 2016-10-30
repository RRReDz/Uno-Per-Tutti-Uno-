/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.connection;

/**
 *
 * @author picardi
 */
public class ServerConnectionException extends RuntimeException {

	/**
	 * Creates a new instance of <code>UnoXTuttiServerException</code> without
	 * detail message.
	 */
	public ServerConnectionException() {
	}

	/**
	 * Constructs an instance of <code>UnoXTuttiServerException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ServerConnectionException(String msg) {
		super(msg);
	}
}
