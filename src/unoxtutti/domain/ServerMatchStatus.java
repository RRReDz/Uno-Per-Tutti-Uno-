/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import unoxtutti.connection.InvalidRequestException;
import unoxtutti.connection.StatusChangedException;
import unoxtutti.utils.MapUtils;

/**
 * Rappresenta lo stato di una partita lato server.
 * @author Davide
 */
public class ServerMatchStatus extends MatchStatus {
    /**
     * Mazzo di pesca.
     */
    private final Deck mazzoPesca;
    
    /**
     * Mani (carte possedute) di ogni giocatore.
     */
    private final HashMap<Player, Collection<Card>> mani;
    
    /**
     * Indica il numero di carte che il giocatore di turno dovrebbe pescare,
     */
    private int cardsToPick;
    
    /**
     * Inizializzazione dello stato di una partita
     * @param players Giocatori partecipanti
     */
    ServerMatchStatus(List<Player> players) {
        super();
        super.trackEvent("La partita è stata avviata.");
        
        /* Inizializzazione del contatore di carte da pescare */
        cardsToPick = 1;
        
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
        
        /* Inizializzazione giocatore */
        currentPlayer = turns.get(0);
        
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
        super.trackEvent("Carta sul tavolo: " + cartaMazzoScarti);
        super.trackEvent("È il turno di " + currentPlayer.getName() + ".");
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

    /**
     * Gestisce una richiesta di scarto carta.
     * @param player Giocatore richiedente
     * @param card Carta che il giocatore desidera scartare
     */
    synchronized void handlePlayCardRequest(Player player, Card card) throws InvalidRequestException, StatusChangedException {
        Collection<Card> mano = mani.get(player);
        
        /* Innazitutto il giocatore deve possedere la carta */
        if(!mano.contains(card)) {
            throw new InvalidRequestException("Non possiedi la carta " + card.toString());
        }
        
        /* Se non è il turno del giocatore, si tratta di un interruzione di turno. */
        if(currentPlayer.equals(player)) {
            /* È il turno del giocatore */
            // TODO: Turno del giocatore corrente
            
        } else {
            /* Interruzione di turno */
            if(cardsToPick != 1) {
                throw new InvalidRequestException("Non puoi interrompere il turno "
                        + "quando un altro giocatore deve pescare delle carte.");
            }
            if(!card.equals(cartaMazzoScarti)) {
                throw new InvalidRequestException("Non è possibile interrompere "
                        + "il turno con un " + card.toString());
            }
            
            /* Il giocatore si impadrona del turno */
            currentPlayer = player;
        }
        
        /* La carta viene scartata */
        mano.remove(card);
        cartaMazzoScarti = card;
        nextPlayer();
        
        /**
         * Se la carta è di tipo "Salta Turno", il giocatore
         * successivo salterà il proprio turno.
         */
        if(cartaMazzoScarti.isSkipTurn()) {
            trackEvent(currentPlayer.getName() + " salta il proprio turno.");
            nextPlayer();
        }
        
        throw new StatusChangedException();
    }

    /**
     * Gestisce una richiesta di tipo "Pesca carta/e".
     * @param player Giocatore richiedente
     */
    synchronized void handlePickCardRequest(Player player) throws InvalidRequestException, StatusChangedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Gestisce una richiesta di tipo "Dubita bluff".
     * @param player Giocatore richiedente
     */
    synchronized void handleCheckBluffRequest(Player player) throws InvalidRequestException, StatusChangedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Gestisce una richiesta di tipo "Dichiara UNO!".
     * @param player Giocatore richiedente
     */
    synchronized void handleDeclareUNORequest(Player player) throws InvalidRequestException, StatusChangedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    /**
     * Inverte la direzionedei turni
     */
    private void changeDirection() {
        switch(turnsDirection) {
            case DIRECTION_FORWARD:
                turnsDirection = MatchStatus.DIRECTION_BACKWARD;
                break;
            case DIRECTION_BACKWARD:
                turnsDirection = MatchStatus.DIRECTION_FORWARD;
                break;
        }
    }
    
    
    /**
     * Assegna il turno al giocatore successivo.
     */
    private void nextPlayer() {
        int currentIndex = turns.indexOf(currentPlayer);
        if(turnsDirection == MatchStatus.DIRECTION_FORWARD) {
            currentIndex += 1;
            if(currentIndex == turns.size()) {
                currentIndex = 0;
            }
        } else {
            currentIndex -= 1;
            if(currentIndex < 0) {
                currentIndex = turns.size() - 1;
            }
        }
        
        currentPlayer = turns.get(currentIndex);
        trackEvent("È il turno di " + currentPlayer.getName() + ".");
    }
}
