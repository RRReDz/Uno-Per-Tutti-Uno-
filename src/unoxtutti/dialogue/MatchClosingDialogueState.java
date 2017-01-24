/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.dialogue;

import unoxtutti.connection.P2PMessage;
import unoxtutti.domain.Match;
import unoxtutti.domain.dialogue.DialogueState;

/**
 *
 * @author Riccardo Rossi
 */
public enum MatchClosingDialogueState implements DialogueState<MatchClosingDialogueState> {
    
    BEFORE_CLOSING, CLOSING, CLOSED, NOT_CLOSED;
    
    @Override
    public MatchClosingDialogueState nextState(P2PMessage msg) {
        String msgName = msg.getName();
        switch (this) {
            case CLOSING:
                if (msgName.equals(Match.MATCH_CLOSING_REPLY_MSG)) {
                    boolean accepted = (Boolean) msg.getParameter(0);
                    if (accepted) {
                        return CLOSED;
                    } else {
                        return NOT_CLOSED;
                    }
                }
            case CLOSED:
            case NOT_CLOSED:
            case BEFORE_CLOSING:
            default:
                return this;
        }
    }
    
}
