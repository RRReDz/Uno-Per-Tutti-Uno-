/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

import unoxtutti.domain.Player;

/**
 * Eccezione lanciata quando è necessario avvertire i giocatori partecipanti
 * del fatto che un giocatore ha vinto la partita.
 * 
 * @author Davide
 */
public class PlayerWonException extends Exception {
    /**
     * Vincitore della partita.
     */
    protected final Player winner;
    
    /**
     * Memorizza il vincitore della partita.
     * 
     * @param winner Giocatore che ha vinto la partita.
     */
    public PlayerWonException(Player winner) {
        super();
        this.winner = winner;
    }
    
    /**
     * Ritorna il vincitore della partita.
     * 
     * @return Giocatore che ha vinto la partita.
     */
    public Player getWinner() {
        return winner;
    }
}
