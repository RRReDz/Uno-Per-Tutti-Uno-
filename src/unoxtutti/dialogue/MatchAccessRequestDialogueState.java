/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.dialogue;

import unoxtutti.connection.P2PMessage;
import unoxtutti.domain.Match;
import unoxtutti.domain.MatchAccessRequest;
import unoxtutti.domain.dialogue.DialogueState;

/**
 * Stati del dialogo per la richiesta, da parte del giocatore, verso il
 * proprietario della stanza, di accesso ad una partita.
 * 
 * @author Davide
 */
public enum MatchAccessRequestDialogueState implements DialogueState<MatchAccessRequestDialogueState> {
    BEFORE_REQUEST, REQUESTED, ADMITTED, REJECTED;

    @Override
    public MatchAccessRequestDialogueState nextState(P2PMessage msg) {
        String msgName = msg.getName();
        switch (this) {
            case REQUESTED:
                if(msgName.equals(MatchAccessRequest.MATCH_ACCESS_REQUEST_REPLY_MSG)) {
                    boolean accepted = (Boolean) msg.getParameter(0);
                    if (accepted) {
                        return ADMITTED;
                    } else {
                        return REJECTED;
                    }
                }
            case ADMITTED:
            case REJECTED:
            case BEFORE_REQUEST:
            default:
                return this;
        }
    }
}
