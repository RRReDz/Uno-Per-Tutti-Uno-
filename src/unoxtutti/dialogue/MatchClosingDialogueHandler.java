/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.dialogue;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;
import unoxtutti.domain.Match;
import unoxtutti.domain.dialogue.BasicDialogueHandler;

/**
 *
 * @author Riccardo Rossi
 */
public class MatchClosingDialogueHandler extends BasicDialogueHandler<MatchClosingDialogueState> {
    
    P2PConnection conn;
    
    /**
     *
     * @param conn
     */
    public MatchClosingDialogueHandler (P2PConnection conn) {
        super(MatchClosingDialogueState.BEFORE_CLOSING);
        this.conn = conn;
    }
    
    /**
     * Inizia il dialogo richiedendo l'avvio e passando allo stato CLOSING.
     * Se l'invio non funziona torna allo stato BEFORE_CLOSING.
     *
     * @param matchName
     * @return <code>true</code> in caso di successo,
     * <code>false</code> altrimenti.
     */
    public boolean startDialogue(String matchName) {
        boolean ret = true;
        /* Listener per rimanere in attesa di messaggi di risposta in questa connessione */
        conn.addMessageReceivedObserver(this, Match.MATCH_CLOSING_REPLY_MSG);
        /* Creazione messaggio di richiesta da inviare */
        P2PMessage msg = new P2PMessage(Match.MATCH_CLOSING_MSG);
        Object[] pars = new Object[]{ matchName };
        msg.setParameters(pars);
        this.setState(MatchClosingDialogueState.CLOSING);
        try {
            conn.sendMessage(msg);
        } catch (PartnerShutDownException ex) {
            this.setState(MatchClosingDialogueState.BEFORE_CLOSING);
            ret = false;
        }
        return ret;
    }
    
    /**
     * Dichiara il dialogo terminato deregistrandosi dalla connessione al fine
     * di non essere più tra gli ascoltatori dei messaggi in arrivo.
     */
    public void concludeDialogue() {
        conn.removeMessageReceivedObserver(this, Match.MATCH_CLOSING_REPLY_MSG);
    }
    
}
