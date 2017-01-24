/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.configuration;

/**
 * Impostazioni e parametri per influenzare il gameplay.
 * @author Davide
 */
public class GameConfig {
    
    /**
     * Numero iniziale di carte di ogni giocatore.
     */
    public static final int STARTING_CARDS = 7;
    
    /**
     * Durata massima di un turno in millisecondi.
     */
    public static final int TURN_MAXIMUM_LENGTH = 30000;
    
    /**
     * Numero massimo di timeouts che un giocatore può fare prima
     * di essere buttato fuori dalla partita.
     */
    public static final int MAXIMUM_TIMEOUTS = 3;
    
}
