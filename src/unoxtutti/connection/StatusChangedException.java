/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

/**
 * Eccezione lanciata quando è necessario avvertire i giocatori partecipanti
 * del cambiamento di stato della partita.
 * @author Davide
 */
public class StatusChangedException extends Exception {
    
    public StatusChangedException() {
        super("Lo stato della partita è cambiato: è necessario avvisare i giocatori.");
    }
    
}
