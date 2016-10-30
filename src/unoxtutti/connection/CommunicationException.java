/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.connection;

/**
 * Eccezione lanciata quando qualche aspetto della comunicazione fra client e 
 * server non funziona (ad esempio il messaggio che arriva e' diverso da quanto
 * ci si aspettava, per tipologia o parametri).
 * @author picardi
 */
public class CommunicationException extends RuntimeException {

	/**
	 * Creates a new instance of <code>UnoXTuttiServerException</code> without
	 * detail message.
	 */
	public CommunicationException() {
	}

	/**
	 * Constructs an instance of <code>UnoXTuttiServerException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public CommunicationException(String msg) {
		super(msg);
	}
}
