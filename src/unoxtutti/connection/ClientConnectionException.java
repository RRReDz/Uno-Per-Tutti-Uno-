/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

/**
 * Rappresenta un'eccezione lanciata nel momento in cui il client
 * non riesce a stabilire una connessione col server.
 * @author picardi
 */
public class ClientConnectionException extends RuntimeException {

	/**
	 * Creates a new instance of <code>UnoXTuttiServerException</code> without
	 * detail message.
	 */
	public ClientConnectionException() {
	}

	/**
	 * Constructs an instance of <code>UnoXTuttiServerException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ClientConnectionException(String msg) {
		super(msg);
	}
}
