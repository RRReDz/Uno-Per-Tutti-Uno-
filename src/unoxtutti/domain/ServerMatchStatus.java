/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.List;

/**
 * Rappresenta lo stato di una partita lato server.
 * @author Davide
 */
public class ServerMatchStatus extends MatchStatus {
    /**
     * Mazzo di pesca
     */
    private final Deck mazzoPesca;
    
    /**
     * Inizializzazione dello stato di una partita
     * @param players Giocatori partecipanti
     */
    ServerMatchStatus(List<Player> players) {
        super(players);
        
        /* Inizializzazione ordine turni */
        
        /* Inizializzazione mazzi */
        mazzoPesca = new Deck();
    }
    
    
}
