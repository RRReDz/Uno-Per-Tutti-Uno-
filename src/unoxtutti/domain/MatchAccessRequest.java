/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import unoxtutti.GiocarePartitaController;
import unoxtutti.connection.P2PConnection;
import unoxtutti.dialogue.MatchAccessRequestDialogueHandler;
import unoxtutti.dialogue.MatchAccessRequestDialogueState;
import unoxtutti.domain.dialogue.DialogueHandler;
import unoxtutti.domain.dialogue.DialogueObserver;
import unoxtutti.utils.DebugHelper;

/**
 * Rappresenta una richiesta di accesso ad una partita
 * @author Davide
 */
public class MatchAccessRequest implements DialogueObserver {
    /**
     * Connessione con il proprietario della stanza in cui ci si trova.
     */
    private final P2PConnection conn;
    
    /**
     * Nome della partita in cui si vuole accedere
     */
    private final String matchName;
    
    /**
     * Indica se la richiesta è stata presa in carico dal RoomServer oppure no
     */
    private boolean requestAccepted;
    
    
    /**
     * DialogueHandler utilizzare per inviare richieste di accesso
     */
    private MatchAccessRequestDialogueHandler accessRequestHandler;
    
    
    /**
     * Memorizza la connessione e il nome della partita a cui si sta
     * cercando di entrare
     * @param connection Connessione con il RoomServer
     * @param matchName Nome della partita desiderata
     */
    public MatchAccessRequest(P2PConnection connection, String matchName) {
        this.conn = connection;
        this.matchName = matchName;
        this.requestAccepted = false;
    }
    
    
    /**
     * Gestisce il cambiamento di stato della richiesta
     * @param source Handler della richiesta
     */
    @Override
    public void updateDialogueStateChanged(DialogueHandler source) {
        if(source.equals(accessRequestHandler)) {
            MatchAccessRequestDialogueState state = accessRequestHandler.getState();
            
            switch(state) {
                case ADMITTED:
                    requestAccepted = true;
                case REJECTED:
                    /** 
                     * Si dice al controller che la richiesta è stata presa in carico,
                     * con successo o meno.
                     */
                    if(requestAccepted) {
                        DebugHelper.log("Risposta da RoomServer: OK! Richiesta di accesso presa in carico.");
                    } else {
                        DebugHelper.log("Risposta da RoomServer: ERR! Richiesta di accesso rifiutata.");
                    }
                    accessRequestHandler.concludeDialogue();
                    GiocarePartitaController.getInstance().wakeUpController();
                    break;
                default:
            }
        }
    }
    
    
    /**
     * Crea e restituisce una richiesta di accesso ad una partita
     * @param matchName Nome della partita a cui si sta tentando di entrare
     * @return Istanza di <code>MatchAccessRequest</code> in caso di successo,
     *          <code>null</code> altrimenti.
     */
    public static MatchAccessRequest createAccessRequest(String matchName) {
        MatchAccessRequest r = new MatchAccessRequest(
                GiocarePartitaController.getInstance().getCurrentRoom().getConnection(),
                matchName
        );
        
        boolean success = r.askPermission();
        if(success) {
            return r;
        }
        return null;
    }
    
    
    /**
     * Avvia il dialogo per l'accesso alla partita.
     * @return <code>true</code> in caso di successo,
     *          <code>false</code> altrimenti
     */
    private boolean askPermission() {
        accessRequestHandler = new MatchAccessRequestDialogueHandler(conn);
        accessRequestHandler.addStateChangeObserver(this);
        return accessRequestHandler.startDialogue(matchName);
    }
    
    /**
     * Indica se la richiesta è stata presa in carico dal RoomServer.
     * @return <code>true</code> se il RoomServer ha preso in carico la richiesta,
     *          <code>false</code> altrimenti.
     */
    public boolean isRequestAccepted() {
        return requestAccepted;
    }
}
