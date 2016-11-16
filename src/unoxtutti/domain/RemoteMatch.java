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
    private final Player owner;
    
    /**
     * DialogueHandler per la creazione di partite.
     */
    private MatchCreationDialogueHandler creationHandler;
    
    /**
     * DialogueHandler per l'avvio della partita
     */
    private MatchStartingDialogueHandler startingHandler;
    
    /**
     * Lista di giocatori all'interno della partita.
     */
    private DefaultListModel<Player> playersList;
    
    /**
     * Indica se la partita è stata avviata o meno.
     */
    private boolean isStarted = false;
    
    /**
     * Costruttore che memorizza le informazioni più importanti.
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
        isStarted = startServerMatch();
        return isStarted;
    }
    
    /**
     * TODO: Implementare metodo
     * @param msg 
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        if(msg.getName().equals(Match.MATCH_UPDATE_MSG)) {
            DebugHelper.log("Ricevuto aggiornamento della stanza da parte di MatchServer.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Gestisce il cambio di stato di un dialogo.
     * @param source DialogueHandler generatore dell'evento.
     */
    @Override
    public void updateDialogueStateChanged(DialogueHandler source) {
        if(source.equals(creationHandler)) {
            MatchCreationDialogueState state = creationHandler.getState();
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
    }
    
    /**
     * Avvia il dialogo con il server per creare una stanza.
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
    

    /**
     * TODO: Javadoc
     * @return 
     */
    private boolean startServerMatch() {
        startingHandler = new MatchStartingDialogueHandler(conn);
        startingHandler.addStateChangeObserver(this);
        /**
         * Serve aggiungere qualche listener dei messaggi qui?
         * TODO: Ricevere OK dal server
         */
        return startingHandler.startDialogue();
    }
}
