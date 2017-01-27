/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import unoxtutti.utils.DebugHelper;
import unoxtutti.utils.TimeUtils;

/**
 * Contiene informazioni riguardanti lo stato di una partita.
 * @author Davide
 */
public class MatchStatus implements Serializable {
    /* Messaggio di aggiornamento */
    public static final String STATUS_UPDATE_MSG = "statusUpdate";
    
    /* Azioni effettuabili dai giocatori */
    public static final String STATUS_PLAY_CARD_MSG = "statusPlayCard";
    public static final String STATUS_PICK_CARD_MSG = "statusPickCard";
    public static final String STATUS_CHECK_BLUFF_MSG = "statusCheckBluff";
    public static final String STATUS_DECLARE_UNO_MSG = "statusDeclareUno";
    public static final String STATUS_CHECK_UNO_DECLARATION = "statisCheckUnoDeclaration";
    
    /* Comunicazione di errori da parte del server */
    public static final String STATUS_ERROR_MESSAGE = "statusErrorMessage";
    
    /**
     * Possibili direzioni di turno
     */
    public static final int DIRECTION_FORWARD = 1;
    public static final int DIRECTION_BACKWARD = 0;
    
    /**
     * Lista dei giocatori in ordine di turno.
     */
    protected ArrayList<Player> turns;
    
    /**
     * Indica la direzione dei turni.
     */
    protected int turnsDirection = MatchStatus.DIRECTION_FORWARD;
    
    /**
     * Giocatore a cui spetta il turno.
     */
    protected Player currentPlayer;
    
    /**
     * È l'ultima carta che è stata scartata.
     */
    protected Card cartaMazzoScarti;
    
    /**
     * Descrizione degli eventi accaduti durante la partita.
     */
    protected Collection<String> events;
    
    /**
     * Indica il numero di carte che il giocatore di turno dovrebbe pescare,
     */
    protected int cardsToPick;
    
    /**
     * Inizializza la lista di eventi
     */
    protected MatchStatus() {
        events = new ArrayList<>(100);
        cardsToPick = 1;
    }
    
    /**
     * Restituisce la direzione dei turni.
     * @return Direzione dei turni.
     */
    public int getDirection() {
        return turnsDirection;
    }
    
    /**
     * Crea una copia dell'istanza corrente.
     * 
     * ATTENZIONE: è molto importante copiare le liste in nuovi oggetti
     * con new ArrayList<>( X ).
     * 
     * @return Stato della partita
     */
    protected MatchStatus creaCopia() {
        MatchStatus upd = new MatchStatus();
        upd.turns = new ArrayList<>(turns);
        upd.currentPlayer = currentPlayer;
        upd.turnsDirection = turnsDirection;
        upd.cartaMazzoScarti = cartaMazzoScarti;
        upd.events = new ArrayList<>(events);
        upd.cardsToPick = cardsToPick;
        return upd;
    }
    
    
    /**
     * Traccia un evento, aggiunge un prefisso con l'ora dell'evento.
     * @param eventMessage Descrizione dell'evento.
     */
    protected void trackEvent(String eventMessage) {
        String prefix = "[" + TimeUtils.getCurrentTimeStamp("HH:mm:ss") + "] ";
        events.add(prefix + eventMessage);
        DebugHelper.log("EVENTO PARTITA: " + eventMessage);
    }

    /**
     * Ritorna la lista dei giocatori in ordine di turno.
     * @return Ordine dei turni.
     */
    public ArrayList<Player> getTurns() {
        return new ArrayList<>(turns);
    }
    
    /**
     * Ritorna il senso di marcio della partita.
     * @return Senso di marcia.
     */
    public int getTurnsDirection() {
        return turnsDirection;
    }

    /**
     * Indica il giocatore a cui spetta il turno.
     * @return Giocatore a cui spetta il turno.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Ritorna la carta attualmente sul tavolo.
     * @return Carta in cima al mazzo degli scarti.
     */
    public Card getCartaMazzoScarti() {
        return cartaMazzoScarti;
    }

    /**
     * Lista degli eventi della partita.
     * @return Lista di eventi accaduti durante la partita.
     */
    public ArrayList<String> getEvents() {
        return new ArrayList<>(events);
    }

    /**
     * Indica il numero di carte da pescare.
     * @return Numero di carte da pescare.
     */
    public int getCardsToPick() {
        return cardsToPick;
    }
}
