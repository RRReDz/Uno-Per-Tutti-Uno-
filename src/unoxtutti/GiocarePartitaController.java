/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti;

import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.domain.MatchAccessRequest;
import unoxtutti.domain.RemoteMatch;
import unoxtutti.domain.RemoteRoom;
import unoxtutti.utils.DebugHelper;

/**
 * Controller singleton
 *
 * @author Riccardo Rossi
 */
public class GiocarePartitaController {

    /**
     * Istanza del controller
     */
    private static GiocarePartitaController instance;

    /**
     * La stanza di appartenenza, viene settata in automatico da
     * <code>GiocareAUnoXTuttiController</code> durante l'accesso e l'uscita.
     *
     * Quando non si è in una stanza, questo valore è <code>null</code>.
     */
    private RemoteRoom currentRoom;

    /**
     * La partita in cui il giocatore si trova correntemente.
     */
    private RemoteMatch currentMatch;

    /**
     * Utilizzata durante l'accesso e la creazione di una partita
     */
    private RemoteMatch matchInLimbo;
    
    /**
     * Utilizzata durante l'invio di una richiesta di acecsso
     */
    private MatchAccessRequest accessRequestInLimbo;

    /**
     * Lock per richieste
     */
    private final Object lock;

    /**
     * Non permette di generare oggetti della classe al di fuori del metodo
     * getInstance()
     */
    private GiocarePartitaController() {
        lock = new Object();
        currentRoom = null;
        currentMatch = null;
        matchInLimbo = null;
        accessRequestInLimbo = null;
    }

    /**
     * Ritorna l'istanza del controller.
     *
     * @return Istanza di <code>GiocarePartitaController</code>
     */
    public static GiocarePartitaController getInstance() {
        if (instance == null) {
            instance = new GiocarePartitaController();
            DebugHelper.log("Creata istanza di GiocarePartitaController.");
        }
        return instance;
    }

    /**
     * Imposta la stanza remota
     *
     * @param newRoom
     */
    protected void setRoom(RemoteRoom newRoom) {
        if (newRoom != null && currentRoom != null) {
            /**
             * Caso dubbio: si sta cercando di passare direttamente da una
             * stanza ad un'altra stanza: non si dovrebbe passare per la lista
             * delle stanze? Ovvero bisogna passare per lo stato in cui il
             * giocatore non si trova in una stanza.
             *
             * currentRoom può quindi cambiare solamente da un'istanza di
             * RemoteRoom a null, e viceversa.
             */
            throw new IllegalStateException(
                    "Impossibile cambiare stanza: il giocatore si trova già in una stanza."
            );
        }
        currentRoom = newRoom;
    }

    /**
     * Crea una partita
     *
     * @param nomePartita Nome della partita
     * @param opzioni
     */
    public void creaPartita(String nomePartita, Object opzioni) {
        if (currentMatch != null) {
            throw new IllegalStateException(
                    "Impossibile creare una partita: il giocatore è già in una partita."
            );
        }

        synchronized (lock) {
            matchInLimbo = RemoteMatch.createRemoteMatch(nomePartita, opzioni);
            if (matchInLimbo != null) {
                try {
                    /**
                     * Si attende che gli altri thread mi avvisino che la
                     * richiesta è terminata.
                     */
                    lock.wait();
                } catch (InterruptedException ex) {
                    matchInLimbo = null;
                    DebugHelper.log("InterruptedException durante la creazione della partita: " + ex.getMessage());
                    Logger.getLogger(GiocarePartitaController.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    /**
                     * Se tutto è andato a buon fine, mi trovo in una stanza,
                     * altrimenti currentMatch rimane null.
                     */
                    currentMatch = matchInLimbo;
                    matchInLimbo = null;
                }
            }
        }
    }

    /**
     * Avvia la partita
     */
    public boolean avviaPartita() {
        if (currentMatch == null) {
            throw new IllegalStateException("Errore: Non esiste alcuna partita associata.");
        }

        synchronized (lock) {
            /**
             * Nel caso la richiesta non abbia avuto successo (lato client) non
             * ha senso attendere una risposta dal server
             */
            boolean isStarting = currentMatch.startMatch();
            if (isStarting) {
                try {
                    /* Attendo una risposta di conferma di avvenuto inizio da parte del server */
                    lock.wait();
                } catch (InterruptedException ex) {
                    DebugHelper.log("InterruptedException durante l'avvio della partita: " + ex.getMessage());
                    Logger.getLogger(GiocarePartitaController.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }
        
        return currentMatch.isStarted();
    }

    /**
     * Ritorna la stanza in cui si trova il giocatore.
     *
     * @return Istanza di <code>RemoteRoom</code>, <code>null</code> se il
     * giocatore non si trova in nessuna stanza.
     */
    public RemoteRoom getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Ritorna la partita in cui si trova il giocatore.
     *
     * @return Istanza di <code>RemoteMatch</code>, <code>null</code> se il
     * giocatore non si trova in nessuna partita.
     */
    public RemoteMatch getCurrentMatch() {
        return currentMatch;
    }

    /**
     * Metodo invocato quando si riceve una risposta positiva da parte del
     * proprietario della stanza, alla richiesta di creazione di una partita.
     * Sveglia il thread che era stato messo in attesa durante "creaPartita".
     *
     * @param room La partita creata.
     */
    public void matchCreationCompleted(RemoteMatch room) {
        synchronized (lock) {
            matchInLimbo = room;
            lock.notifyAll();
        }
    }

    /**
     * Metodo invocato quando si riceve una risposta negativa da parte del
     * proprietario della stanza, alla richiesta di creazione di una partita.
     *
     * Sveglia il thread che era stato messo in attesa durante "creaPartita".
     */
    public void matchCreationFailed() {
        synchronized (lock) {
            matchInLimbo = null;
            lock.notifyAll();
        }
    }

    /**
     * @return true se giocatore già all'interno di una partita, false
     * altrimenti
     */
    public boolean inPartita() {
        return (currentMatch != null);
    }

    /**
     * Sblocca il client in attesa di una risposta di conferma di avvio
     * da parte del server
     */
    public void matchStartEnded() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
    
    
    /**
     * Richiede l'accesso ad una partita
     * @param matchName Nome della partita
     * @return <code>true</code> se la richiesta è stata presa in carico
     *          con successo, <code>false</code> altrimenti.
     */
    public boolean richiediIngresso(String matchName) {
        if (currentMatch != null) {
            throw new IllegalStateException(
                    "Impossibile creare una partita: il giocatore è già in una partita."
            );
        }
        
        boolean success = false;
        synchronized (lock) {
            accessRequestInLimbo = MatchAccessRequest.createAccessRequest(matchName);
            if (accessRequestInLimbo != null) {
                try {
                    /**
                     * Si attende che gli altri thread mi avvisino che la
                     * richiesta è terminata.
                     */
                    lock.wait();
                    success = accessRequestInLimbo.isRequestAccepted();
                } catch (InterruptedException ex) {
                    DebugHelper.log("InterruptedException durante una richiesta di ingresso: " + ex.getMessage());
                    Logger.getLogger(GiocarePartitaController.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    accessRequestInLimbo = null;
                }
            }
        }
        return success;
    }
    
    
    /**
     * La richiesta di accesso ad una partita è stata analizzata dal
     * proprietario della stanza.
     */
    public void matchAccessRequestTakenCareOf() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
