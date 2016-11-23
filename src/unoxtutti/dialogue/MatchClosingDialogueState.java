/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.dialogue;

import unoxtutti.connection.P2PMessage;
import unoxtutti.domain.dialogue.DialogueState;
import unoxtutti.domain.Match;

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
            case BEFORE_CLOSING:
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
            case CLOSING:
            default:
                return this;
        }
    }
    
}
