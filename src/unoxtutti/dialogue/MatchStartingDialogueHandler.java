/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.dialogue;

import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;
import unoxtutti.domain.Match;
import unoxtutti.domain.dialogue.BasicDialogueHandler;
import unoxtutti.utils.DebugHelper;

/**
 * TODO
 * @author Riccardo Rossi
 */
public class MatchStartingDialogueHandler extends BasicDialogueHandler<MatchStartingDialogueState>{

    private final P2PConnection p2pConn;
    
    public MatchStartingDialogueHandler(P2PConnection conn) {
        super(MatchStartingDialogueState.BEFORE_STARTING);
        p2pConn = conn;
    }
    
    /**
     * Inizia il dialogo richiedendo l'avvio e passando allo stato STARTING.
     * Se l'invio non funziona torna allo stato BEFORE_STARTING.
     *
     * @param matchName
     * @param TODO
     * @param TODO
     * @return TODO
     */
    public boolean startDialogue(String matchName) {
        /* Listener per la risposta di avvio partita dal server*/
        p2pConn.addMessageReceivedObserver(this, Match.MATCH_STARTING_REPLY_MSG);
        
        P2PMessage msg = new P2PMessage(Match.MATCH_STARTING_MSG);
        Object[] pars = new Object[]{matchName};
        msg.setParameters(pars);
        this.setState(MatchStartingDialogueState.STARTING);
        try {
            p2pConn.sendMessage(msg);
        } catch (PartnerShutDownException ex) {
            this.setState(MatchStartingDialogueState.BEFORE_STARTING);
            return false;
        }
        return true;
    }
    
    /**
     * Dichiara il dialogo terminato deregistrandosi dalla connessione al fine
     * di non essere più tra gli ascoltatori dei messaggi in arrivo.
     */
    public void concludeDialogue() {
        p2pConn.removeMessageReceivedObserver(this, Match.MATCH_STARTING_REPLY_MSG);
    }
    
}
