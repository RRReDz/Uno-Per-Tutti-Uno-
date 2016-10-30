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
public class ServerCreationException extends RuntimeException {

	/**
	 * Creates a new instance of <code>UnoXTuttiServerException</code> without
	 * detail message.
	 */
	public ServerCreationException() {
	}

	/**
	 * Constructs an instance of <code>UnoXTuttiServerException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ServerCreationException(String msg) {
		super(msg);
	}
}
