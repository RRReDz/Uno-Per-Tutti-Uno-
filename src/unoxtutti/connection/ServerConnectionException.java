/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
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
