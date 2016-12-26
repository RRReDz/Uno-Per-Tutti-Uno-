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
     * Stanza di appartenenza, usata per recuperare le connessioni
     */
    private final ServerRoom room;
    
    /**
     * Mazzo di pesca
     */
    private final Deck mazzoPesca;
    
    /**
     * Inizializzazione dello stato di una partita
     * @param players Giocatori partecipanti
     * @param room Stanza di appartenenza
     */
    ServerMatchStatus(List<Player> players, ServerRoom room) {
        super(players);
        this.room = room;
        
        /* Inizializzazione ordine turni */
        
        /* Inizializzazione mazzi */
        mazzoPesca = new Deck();
    }
    
    
}
