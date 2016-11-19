/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import unoxtutti.dialogue.MatchCreationDialogueState;
import unoxtutti.dialogue.MatchCreationDialogueHandler;
import unoxtutti.dialogue.MatchStartingDialogueHandler;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import unoxtutti.GiocarePartitaController;
import unoxtutti.UnoXTutti;
import unoxtutti.connection.CommunicationException;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.dialogue.MatchAccessRequestDialogueHandler;
import unoxtutti.dialogue.MatchAccessRequestDialogueState;
import unoxtutti.dialogue.MatchStartingDialogueState;
import unoxtutti.domain.dialogue.DialogueHandler;
import unoxtutti.domain.dialogue.DialogueObserver;
import unoxtutti.utils.DebugHelper;

/**
 * Rappresenta una partita dal punto di vista dei client.
 * @author Davide
 */
public class RemoteMatch extends Match implements MessageReceiver, DialogueObserver {
    /**
     * Connessione con il proprietario della stanza in cui ci si trova.
     */
    private final P2PConnection conn;
    
    /**
     * Giocatore proprietario della partita
     */
    private Player owner;
    
    /**
     * DialogueHandler per la creazione di partite.
     */
    private MatchCreationDialogueHandler creationHandler;
    
    /**
     * DialogueHandler per l'avvio della partita
     */
    private MatchStartingDialogueHandler startingHandler;
    
    /**
     * DialogueHandler utilizzare per inviare richieste di accesso
     */
    private MatchAccessRequestDialogueHandler accessRequestHandler;
    
    /**
     * Lista di giocatori all'interno della partita.
     */
    private final DefaultListModel<Player> playersList;
    
    /**
     * Indica se la partita è stata avviata o meno.
     */
    private boolean isStarted = false;
    
    /**
     * Costruttore che memorizza le informazioni più importanti.
     * Questo costrutto viene utilizzato durante la creazione di una partita.
     * @param connectionToRoomHost Connessione con il proprietario della stanza.
     * @param matchName Nome della partita desiderato.
     * @param options Opzioni della partita.
     */
    private RemoteMatch(P2PConnection connectionToRoomHost, String matchName, Object options) {
        super(matchName, options);
        conn = connectionToRoomHost;
        owner = UnoXTutti.theUxtController.getPlayer();
        playersList = new DefaultListModel<>();
    }
    
    /**
     * Costruttore utilizzato quando si richiede l'accesso ad una partita.
     * Mancano infatti alcune informazioni che dovranno essere aggiunte in
     * futuro, ovvero all'ingresso della partita.
     * @param connectionToRoomHost Connessione con il proprietario della stanza.
     * @param matchName Nome della in cui si vuole entrare.
     */
    private RemoteMatch(P2PConnection connectionToRoomHost, String matchName) {
        super(matchName);
        conn = connectionToRoomHost;
        owner = null;
        playersList = new DefaultListModel<>();
    }
    
    /**
     * Tenta di creare una partita all'interno della stanza indicata.
     * @param matchName Nome della partita
     * @param options Opzioni di creazione della partita
     * @return Istanza di <code>RemoteMatch</code>, <code>null</code> in caso di
     * fallimento.
     */
    public static RemoteMatch createRemoteMatch(String matchName, Object options) {
        RemoteMatch m = new RemoteMatch(
                GiocarePartitaController.getInstance().getCurrentRoom().getConnection(),
                matchName,
                options
        );

        boolean success = m.create();
        if(success) {
            return m;
        }
        return null;
    }
    
    
    /**
     * Set della variabile di avvio della partita.
     * @return <code>true</code> se la partita viene avviata con successo,
     *          <code>false</code> altrimenti
     */
    public boolean startMatch() {
        return startServerMatch();
    }
    
    /**
     * Viene richiamato ogni qualvolta la P2PConnection notifica
     * questa istanza di un messaggio di tipo "MATCH_UPDATE_MSG"
     * @param msg messaggio ricevuto 
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        if(msg.getName().equals(Match.MATCH_UPDATE_MSG)) {
            DebugHelper.log("Ricevuto aggiornamento della partita da parte di MatchServer.");
            try {
                /* Aggiornamento lista giocatori */
                ArrayList<Player> players = (ArrayList<Player>) msg.getParameter(0);
                playersList.removeAllElements();
                players.forEach((p) -> {
                    playersList.addElement(p);
                });
            } catch (ClassCastException ex) {
                throw new CommunicationException("Wrong parameter type in message " + msg.getName());
            }
        }
    }
    
    /**
     * Gestisce il cambio di stato di un dialogo.
     * @param source DialogueHandler generatore dell'evento.
     */
    @Override
    public void updateDialogueStateChanged(DialogueHandler source) {
        /* Handler della creazione della partita */
        if(source.equals(creationHandler)) {
            handleMatchCreationRequest(creationHandler);
        }
        /* Handler per l'invio di una richiesta di accesso ad una partita a RoomHost */
        else if(source.equals(accessRequestHandler)) {
            handleMatchAccessRequest(accessRequestHandler);
        }
        /* Handler dell'avvio della partita */
        else if(source.equals(startingHandler)) {
            handleMatchStartingRequest(startingHandler);
        }
    }
    
    
    /**
     * Gestisce il cambiamento di stato dell'handler per la creazione
     * di partite.
     * @param source Handler della richiesta
     */
    private void handleMatchCreationRequest(MatchCreationDialogueHandler source) {
        MatchCreationDialogueState state = source.getState();
        switch (state) {
            case ADMITTED:
                DebugHelper.log("Risposta da RoomServer: OK! La partita è stata creata.");
                creationHandler.concludeDialogue();
                GiocarePartitaController.getInstance().matchCreationCompleted(this);
                break;
            case REJECTED:
                DebugHelper.log("Risposta da RoomServer: ERR! Impossibile creare la partita.");
                creationHandler.concludeDialogue();
                GiocarePartitaController.getInstance().matchCreationFailed();
                break;
            default:
        }
    }
    
    /**
     * Gestisce il cambiamento di stato dell'handler per l'invio di richieste
     * di accesso in partite al RoomServer.
     * @param source Handler della richiesta
     */
    private void handleMatchAccessRequest(MatchAccessRequestDialogueHandler source) {
        MatchAccessRequestDialogueState state = source.getState();
        
        
        switch(state) {
            case ADMITTED:
            case REJECTED:
                /** 
                 * Si dice al controller che la richiesta è stata presa in carico,
                 * con successo o meno.
                 */
                boolean accepted = state == MatchAccessRequestDialogueState.ADMITTED;
                if(accepted) {
                    DebugHelper.log("Risposta da RoomServer: OK! Richiesta di accesso presa in carico.");
                } else {
                    DebugHelper.log("Risposta da RoomServer: ERR! Richiesta di accesso rifiutata.");
                }
                GiocarePartitaController.getInstance().matchAccessRequestTakenCareOf(accepted);
                break;
            default:
        }
    }
    
    /**
     * Gestisce il cambiamento di stato dell'handler per l'avvio della partita.
     * @param source Handler della richiesta 
     */
    private void handleMatchStartingRequest(MatchStartingDialogueHandler source) {
        MatchStartingDialogueState state = source.getState();
        switch(state) {
            case STARTED:
                /**
                 * Solo in questo caso abbiamo una vera conferma se
                 * il ServerMatch è partito o meno.
                 */
                isStarted = true;
                DebugHelper.log("Risposta da MatchServer: OK! La partita è stata avviata.");
                break;
            case NOT_STARTED:
                /**
                 * Solo in questo caso abbiamo una vera conferma se
                 * il ServerMatch è partito o meno.
                 */
                isStarted = false;
                DebugHelper.log("Risposta da MatchServer: ERR! Impossibile avviare la partita.");
                break;
        }
        creationHandler.concludeDialogue();
        GiocarePartitaController.getInstance().matchStartEnded();
    }
    
    /**
     * Avvia il dialogo con il server per creare una partita.
     * @return <code>true</code> se il dialogo è stato avviato con successo,
     *          <code>false</code> altrimenti.
     */
    private boolean create() {
        creationHandler = new MatchCreationDialogueHandler(conn);
        conn.addMessageReceivedObserver(this, Match.MATCH_UPDATE_MSG);
        creationHandler.addStateChangeObserver(this);
        return creationHandler.startDialogue(owner, matchName, options);
    }
    
    /**
     * Recupera la lista dei giocatori all'interno della partita
     * @return playersList lista di giocatori
     */
    public ListModel<Player> getPlayersAsList() {
        return playersList;
    }
    
    
    /**
     * Indica se il giocatore corrente è il proprietario della partita
     * @return <code>true</code> se l'utente è il proprietario della partita,
     *          <code>false</code> altrimenti
     */
    public boolean amITheOwner() {
        return owner != null && owner == UnoXTutti.theUxtController.getPlayer();
    }
    

    /** Avvia il dialogo con il server per avviare una partita.
     * @return <code>true</code> se il dialogo è stato avviato con successo,
     *          <code>false</code> altrimenti.
     **/
    private boolean startServerMatch() {
        startingHandler = new MatchStartingDialogueHandler(conn);
        startingHandler.addStateChangeObserver(this);
        /**
         * TODO: Ricevere OK dal server
         */
        return startingHandler.startDialogue(matchName);
    }

    /**
     * @return isStarted <code>true</code> se il match è stato avviato
     * <code>false</code> altrimenti
     */
    public boolean isStarted() {
        return isStarted;
    }
    
    
    /**
     * Richiedi al proprietario della stanza il permesso di entrare in una
     * partita di cui il giocatore conosce solamente il nome.
     * @param matchName Nome della partita
     * @return <code>true</code> se il dialogo viene avviato con successo,
     *          <code>false</code> altrimenti
     */
    public static RemoteMatch sendAccessRequest(String matchName) {
        RemoteMatch m = new RemoteMatch(
                GiocarePartitaController.getInstance().getCurrentRoom().getConnection(),
                matchName
        );
        
        boolean success = m.askPermission();
        if(success) {
            return m;
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
}
