/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
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
