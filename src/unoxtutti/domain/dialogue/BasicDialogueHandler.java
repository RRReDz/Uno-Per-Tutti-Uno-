/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain.dialogue;

import unoxtutti.connection.P2PMessage;

/**
 * La classe BasicDialogueHandler rapresenta uno Handler che demanda la gestione
 * dei cambi di stato agli stati stessi (ogni stato "sa" quale &egrave; il suo
 * stato successivo).
 *
 * @author picardi
 * @param <U> La sottoclasse di DialogueState che rappresenta gli stati di
 * questo particolare handler
 */
public class BasicDialogueHandler<U extends DialogueState<U>> extends DialogueHandler<U> {

    public BasicDialogueHandler(U initialState) {
        super(initialState);
    }

    @Override
    protected final boolean evolveState(P2PMessage msg) {
        U cur = getState();
        this.setState(cur.nextState(msg));
        return (!this.getState().equals(cur));
    }

}
