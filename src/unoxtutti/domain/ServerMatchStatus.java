/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;
import unoxtutti.utils.MapUtils;

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
        super();
        super.trackEvent("La partita è stata avviata.");
        
        /* Inizializzazione ordine turni */
        /* Mazzo temporaneo per stabilire l'ordine dei turni*/
        Deck rndDeck = new Deck();
        Map<Player, Card> randomCards = new HashMap<>();
        players.forEach((p) -> {
            Card c = rndDeck.pescaCarta();
            randomCards.put(p, c);
            String eventMessage = p.getName() + " ha pescato " + c + ", valore: " + c.getCardValue();
            super.trackEvent(eventMessage);
        });
        
        /* Si aggiorna l'ordine della lista dei turni */
        super.trackEvent("Inizializzazione ordine dei turni...");
        turns = new ArrayList<>();
        Map<Player, Card> orderedTurns = MapUtils.sortByValueReverseOrder(randomCards);
        orderedTurns.forEach((player, card) -> {
            turns.add(player);
        });
        
        /* Inizializzazione mazzi */
        mazzoPesca = new Deck();
        
        /* Inizializzazioni mani */
        super.trackEvent("Distribuzione delle carte...");
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
        super.trackEvent("Le carte sono state distribuite.");
        
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
