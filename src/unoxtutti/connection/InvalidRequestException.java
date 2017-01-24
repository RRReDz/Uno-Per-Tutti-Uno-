/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

/**
 * Eccezzione lanciata al fine di gestire una richiesta non valida da parte 
 * del giocatore verso il MatchServer.
 * @author Davide
 */
public class InvalidRequestException extends Exception {
    
    /**
     * Messaggio di errore da segnalare al giocatore.
     * @param errorMessage Descrizione dell'errore.
     */
    public InvalidRequestException(String errorMessage) {
        super(errorMessage);
    }
    
}
