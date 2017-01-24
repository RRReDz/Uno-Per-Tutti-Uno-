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

/**
 * Handler che gestisce il dialogo tra client e proprietario di una stanza
 * quando il client desidera entrare in una partita all'interno della stanza.
 * @author Davide
 */
public class MatchAccessRequestDialogueHandler extends BasicDialogueHandler<MatchAccessRequestDialogueState> {
    P2PConnection connection;
    
    /**
     * Crea un handler per una richiesta di accesso ad una partita.
     * L'handler è inizialmente in stato BEFORE_REQUEST.
     *
     * @param connection La connessione da utilizzare per l'invio di messaggi.
     */
    public MatchAccessRequestDialogueHandler(P2PConnection connection) {
        super(MatchAccessRequestDialogueState.BEFORE_REQUEST);
        this.connection = connection;
    }
    
    
    /**
     * Inizia il dialogo richiedendo l'accesso alla partita e passando
     * allo stato REQUESTED.
     * Se l'invio non funziona torna allo stato BEFORE_REQUEST.
     *
     * @param matchName Nome della partita
     * @return true se la richiesta è partita correttamente, false se c'è stato
     * un problema di comunicazione.
     */
    public boolean startDialogue(String matchName) {
        boolean ret = true;
        connection.addMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
        P2PMessage msg = new P2PMessage(Match.MATCH_ACCESS_REQUEST_MSG);
        Object[] pars = new Object[]{matchName};
        
        /**
         * TODO: Passare come argomento le opzioni.
         * Se si vogliono passare come argomento le opzioni, queste devono
         * essere serializzabili.
         */
        msg.setParameters(pars);
        this.setState(MatchAccessRequestDialogueState.REQUESTED);
        try {
            connection.sendMessage(msg);
        } catch (PartnerShutDownException ex) {
            this.setState(MatchAccessRequestDialogueState.BEFORE_REQUEST);
            ret = false;
        }
        return ret;
    }
    
    
    /**
     * Dichiara il dialogo terminato deregistrandosi dalla connessione al fine
     * di non essere più tra gli ascoltatori dei messaggi in arrivo.
     */
    public void concludeDialogue() {
        connection.removeMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
    }
}
