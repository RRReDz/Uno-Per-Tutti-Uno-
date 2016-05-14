/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.connection;

/**
 * Eccezione lanciata quando o il client o il server chiude inaspettatamente
 * la comunicazione.
 * @author picardi
 */
public class PartnerShutDownException extends Exception {

	/**
	 * Creates a new instance of <code>UnoXTuttiServerException</code> without
	 * detail message.
	 */
	public PartnerShutDownException() {
	}

	/**
	 * Constructs an instance of <code>UnoXTuttiServerException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public PartnerShutDownException(String msg) {
		super(msg);
	}
}
