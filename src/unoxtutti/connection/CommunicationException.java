/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

/**
 * Eccezione lanciata quando qualche aspetto della comunicazione fra client e
 * server non funziona (ad esempio il messaggio che arriva è diverso da quanto
 * ci si aspettava, per tipologia o parametri).
 *
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
