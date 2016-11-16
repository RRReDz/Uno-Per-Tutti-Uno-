/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.dialogue;

import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;
import unoxtutti.domain.Match;
import unoxtutti.domain.Player;
import unoxtutti.domain.dialogue.BasicDialogueHandler;

/**
 * Handler che gestisce il dialogo tra client e proprietario di una stanza
 * quando il client desidera creare una partita all'interno della stanza.
 * @author Davide
 */
public class MatchCreationDialogueHandler extends BasicDialogueHandler<MatchCreationDialogueState> {
    P2PConnection connection;
    
    /**
     * Crea un handler per una richiesta di creazione partita.
     * L'handler è inizialmente in stato BEFORE_REQUEST.
     *
     * @param connection La connessione da utilizzare per l'invio di messaggi.
     */
    public MatchCreationDialogueHandler(P2PConnection connection) {
        super(MatchCreationDialogueState.BEFORE_REQUEST);
        this.connection = connection;
    }
    
    
    /**
     * Inizia il dialogo richiedendo la creazione della partita e passando
     * allo stato REQUESTED.
     * Se l'invio non funziona torna allo stato BEFORE_REQUEST.
     *
     * @param owner Proprietario della partita
     * @param matchName Nome della partita desiderato
     * @param options Opzioni
     * @return true se la richiesta è partita correttamente, false se c'è stato
     * un problema di comunicazione.
     */
    public boolean startDialogue(Player owner, String matchName, Object options) {
        boolean ret = true;
        connection.addMessageReceivedObserver(this, Match.MATCH_CREATION_REPLY_MSG);
        P2PMessage msg = new P2PMessage(Match.MATCH_CREATION_REQUEST_MSG);
        Object[] pars = new Object[]{owner, matchName};
        
        /**
         * TODO: Passare come argomento le opzioni.
         * Se si vogliono passare come argomento le opzioni, queste devono
         * essere serializzabili.
         */
        msg.setParameters(pars);
        this.setState(MatchCreationDialogueState.REQUESTED);
        try {
            connection.sendMessage(msg);
        } catch (PartnerShutDownException ex) {
            this.setState(MatchCreationDialogueState.BEFORE_REQUEST);
            ret = false;
        }
        return ret;
    }
    
    
    /**
     * Dichiara il dialogo terminato deregistrandosi dalla connessione al fine
     * di non essere più tra gli ascoltatori dei messaggi in arrivo.
     */
    public void concludeDialogue() {
        connection.removeMessageReceivedObserver(this, Match.MATCH_CREATION_REPLY_MSG);
    }
}
