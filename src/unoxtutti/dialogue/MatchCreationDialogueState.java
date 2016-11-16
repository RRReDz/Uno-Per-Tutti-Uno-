/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.dialogue;

import unoxtutti.connection.P2PMessage;
import unoxtutti.domain.Match;
import unoxtutti.domain.dialogue.DialogueState;

/**
 * Stati del dialogo per la creazione di una partita.
 * La conversazione è molto semplice e, concettualmente, uguale a quella
 * per l'ingresso in una stanza.
 * Gli stati sono infatti gli stessi di <code>RoomEntranceDialogueState</code>:
 * cambia solo la stringa su cui si esegue la verifica per la ricezione dei
 * messaggi.
 * 
 * @author Davide
 */
public enum MatchCreationDialogueState implements DialogueState<MatchCreationDialogueState> {
    BEFORE_REQUEST, REQUESTED, ADMITTED, REJECTED;

    @Override
    public MatchCreationDialogueState nextState(P2PMessage msg) {
        String msgName = msg.getName();
        switch (this) {
            case REQUESTED:
                if(msgName.equals(Match.MATCH_CREATION_REPLY_MSG)) {
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
