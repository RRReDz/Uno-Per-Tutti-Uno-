/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Contiene informazioni riguardanti lo stato di una partita.
 * @author Davide
 */
public abstract class MatchStatus {
    /* Messaggio di aggiornamento */
    public static final String STATUS_UPDATE_MSG = "statusUpdate";
    
    /**
     * Possibili direzioni di turno
     */
    protected static final int DIRECTION_FORWARD = 1;
    protected static final int DIRECTION_BACKWARD = 0;
    
    /**
     * Lista dei giocatori in ordine di turno.
     */
    protected ArrayList<Player> turns;
    
    /**
     * Indica la direzione dei turni.
     * <code>true</code> significa avanti,
     * <code>false</code> significa indietro.
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
     * Inizializza lo stato di una partita
     * @param players Lista dei giocatori partecipanti
     */
    protected MatchStatus(List<Player> players) {
        turns = new ArrayList<>();
        turns.addAll(players);
    }
    
    /**
     * Restituisce la direzione dei turni.
     * @return Direzione dei turni.
     */
    public int getDirection() {
        return turnsDirection;
    }
    
    /**
     * Inverte la direzionedei turni
     */
    protected void changeDirection() {
        switch(turnsDirection) {
            case DIRECTION_FORWARD:
                turnsDirection = MatchStatus.DIRECTION_BACKWARD;
                break;
            case DIRECTION_BACKWARD:
                turnsDirection = MatchStatus.DIRECTION_FORWARD;
                break;
        }
    }
}
