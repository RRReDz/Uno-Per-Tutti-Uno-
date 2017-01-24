/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.TimerTask;

/**
 * Task che controlla se il giocatore ha terminato il proprio turno.
 * 
 * Dopo GameConfig.TURN_MAXIMUM_LENGTH millisecondi il task controlla
 * se ci sono stati aggiornamenti:
 * - se ci sono stati aggiornamenti, bene
 * - se non ci sono stati aggiornamenti il giocatore del turno corrente
 * ci sta mettendo troppo tempo ed il suo turno deve essere terminato.
 * 
 * Il task non è periodico: viene creato un task diverso
 * per ogni aggiornamento.
 * 
 * @author Davide
 */
public class TimeoutChecker extends TimerTask {
    /**
     * Partita su cui agisce il timer.
     */
    protected final ServerMatch match;
    
    /**
     * Identificativo dell'aggiornamento al momento della creazione.
     */
    protected final int UPDATE_ID;
    
    /**
     * Memorizza la partita corrente e si segna
     * l'identificativo dell'aggiornamento.
     * @param match 
     */
    public TimeoutChecker(ServerMatch match) {
        this.match = match;
        this.UPDATE_ID = match.updateId;
    }
    
    /**
     * Esegue il controllo descritto sopra.
     */
    @Override
    public void run() {
        synchronized(match) {
            if(match.updateId == UPDATE_ID && !match.ended) {
                match.currentPlayerTimedOut();
            }
        }
        
        /* Giusto in caso di creazione errata del task. */
        cancel();
    }
    
}
