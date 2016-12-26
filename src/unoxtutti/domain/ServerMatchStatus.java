/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
     * Mani (carte possedute) di ogni giocatore
     */
    private final HashMap<Player, Collection<Card>> mani;
    
    /**
     * Inizializzazione dello stato di una partita
     * @param players Giocatori partecipanti
     */
    ServerMatchStatus(List<Player> players) {
        super(players);
        
        /* Inizializzazione ordine turni */
        
        /* Inizializzazione mazzi */
        mazzoPesca = new Deck();
        
        /* Inizializzazioni mani */
        mani = new HashMap<>();
        players.forEach((p) -> {
            mani.put(p, new ArrayList<>());
        });
        
        /* Si pescano 7 carte per ogni giocatore */
        for(int i = 0; i < 7; i++) {
            players.forEach((p) -> {
                mani.get(p).add(mazzoPesca.pescaCarta());
            });
        }
        
        /* Carta iniziale sul tavolo */
        cartaMazzoScarti = mazzoPesca.pescaCarta();
    }
    
    
    /**
     * Recupera le carte nella mano di un giocatore.
     * @param player Giocatore
     * @return Carte possedute dal giocatore
     */
    public Collection<Card> getCardsOfPlayer(Player player) {
        if(!turns.contains(player)) {
            throw new IllegalArgumentException("Il giocatore non è presente nella stanza.");
        }
        
        /* Carte possedute dal giocatore */
        return mani.get(player);
    }
}
