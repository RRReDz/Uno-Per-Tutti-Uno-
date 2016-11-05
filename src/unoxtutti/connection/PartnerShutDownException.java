/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

/**
 * Eccezione lanciata quando o il client o il server chiude inaspettatamente la
 * comunicazione.
 *
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
