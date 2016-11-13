/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import unoxtutti.GiocarePartitaController;
import unoxtutti.UnoXTutti;
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
     * Costruttore che memorizza le informazioni più importanti.
     * @param connectionToRoomHost Connessione con il proprietario della stanza.
     * @param matchName Nome della partita desiderato.
     * @param options Opzioni della partita.
     */
    private RemoteMatch(P2PConnection connectionToRoomHost, String matchName, Object options) {
        super(matchName, options);
        conn = connectionToRoomHost;
        owner = UnoXTutti.theUxtController.getPlayer();
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
     * TODO: Implementare metodo
     * @param msg 
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
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
                    DebugHelper.log("REPLY from ROOM: PARTITA_CREATA");
                    creationHandler.concludeDialogue();
                    GiocarePartitaController.getInstance().matchCreationCompleted(this);
                    break;
                case REJECTED:
                    DebugHelper.log("REPLY from ROOM: PARTITA_NON_VALIDA");
                    creationHandler.concludeDialogue();
                    GiocarePartitaController.getInstance().matchCreationFailed();
                    break;
                default:
            }
        }
    }
    
    /**
     * TODO: Implementare metodo
     * @return 
     */
    private boolean create() {
        creationHandler = new MatchCreationDialogueHandler(conn);
        conn.addMessageReceivedObserver(this, Match.MATCH_UPDATE_MSG);
        creationHandler.addStateChangeObserver(this);
        return creationHandler.startDialogue(owner, matchName, options);
    }
    
}
