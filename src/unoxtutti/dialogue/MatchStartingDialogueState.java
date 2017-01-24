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
public enum MatchStartingDialogueState implements DialogueState<MatchStartingDialogueState> {
    
    BEFORE_STARTING, STARTING, STARTED, NOT_STARTED;

    @Override
    public MatchStartingDialogueState nextState(P2PMessage msg) {
        String msgName = msg.getName();
        switch (this) {
            case STARTING:
                if (msgName.equals(Match.MATCH_STARTING_REPLY_MSG)) {
                    boolean started = (Boolean) msg.getParameter(0);
                    if (started) {
                        return STARTED;
                    } else {
                        return NOT_STARTED;
                    }
                }
            case STARTED:
            case NOT_STARTED:
            case BEFORE_STARTING:
            default:
                return this;
        }
    }
    
}
