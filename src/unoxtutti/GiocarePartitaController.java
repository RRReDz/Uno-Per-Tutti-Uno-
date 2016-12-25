/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti;

import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.connection.CommunicationException;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.domain.Match;
import unoxtutti.domain.MatchAccessRequest;
import unoxtutti.domain.Player;
import unoxtutti.domain.RemoteMatch;
import unoxtutti.domain.RemoteRoom;
import unoxtutti.gui.UnoXTuttiGUI;
import unoxtutti.utils.DebugHelper;

/**
 * Controller singleton
 *
 * @author Riccardo Rossi
 */
public class GiocarePartitaController implements MessageReceiver {
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
                    
                    /**
                     * Se la partita è stata creata con successo, mi metto in
                     * ascolto di messaggi di ingresso.
                     */
                    if(currentMatch != null) {
                        playerCreatedAMatch();
                    }
                }
            }
        }
    }

    /**
     * Avvia la partita
     * @return 
     */
    public boolean avviaPartita() {
        /* Non esiste alcuna partita */
        if (currentMatch == null) {
            throw new IllegalStateException("Errore: Non esiste alcuna partita associata.");
        }
        /* Se la partita è già stata avviata */
        if(currentMatch.isStarted()) {
            throw new IllegalStateException("Errore: Questa partita è già stata avviata.");
        }
            
        synchronized (lock) {
            boolean isStarting = currentMatch.startMatch();
            /**
             * Nel caso la richiesta non abbia avuto successo (lato client) non
             * ha senso attendere una risposta dal server
             */
            if (isStarting) {
                try {
                    /* Attendo una risposta di conferma di avvenuto inizio da parte del server */
                    lock.wait();
                } catch (InterruptedException ex) {
                    DebugHelper.log("InterruptedException durante l'avvio della partita: " + ex.getMessage());
                    return currentMatch.isStarted();
                }
            }
            
            return currentMatch.isStarted();
        }
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
     * @param match La partita creata.
     */
    public void matchCreationCompleted(RemoteMatch match) {
        synchronized (lock) {
            matchInLimbo = match;
            lock.notifyAll();
        }
    }

    /**
     * Metodo invocato quando si riceve una risposta negativa da parte del
     * proprietario della stanza, alla richiesta di creazione di una partita.
     *
     * Sveglia il thread che era stato messo in attesa durante "creaPartita".
     * 
     * @param match La partita creata.
     */
    public void matchCreationFailed(RemoteMatch match) {
        synchronized (lock) {
            /**
             * Dato che la creazione è fallita, 
             * si rimuove il listener per aggiornamenti
             */
            currentRoom.getConnection().removeMessageReceivedObserver(match, Match.MATCH_UPDATE_MSG);
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
     * Risveglia tutti i thread bloccati sulla wait() in attesa di 
     * una risposta dal server sull'operazione richiesta
     */
    public void wakeUpController() {
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
                    if(success) {
                        /**
                         * Se la richiesta è stata presa in carico, mi metto
                         * in ascolto di notifiche di accettazione.
                         */
                        currentRoom.getConnection()
                                .addMessageReceivedObserver(this, Match.MATCH_ACCESS_SUCCESS_NOTIFICATION_MSG);
                    }
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
     * Metodo dei contratti delle operazioni utilizzato per chiudere la partita.
     * @return <code>true</code> in caso di successo,
     * <code>false</code> altrimenti.
     */
    public boolean chiudiPartita() {
        if (currentMatch == null) {
            /* Partita inesistente */
            throw new IllegalStateException("Errore: non esiste alcuna partita associata.");
        } else if (currentMatch.isStarted()) {
            /* Il match è già stato chiuso */
            throw new IllegalStateException("Errore: la partita è già stata avviata.");
        }
        
        synchronized(lock) {
            boolean isClosing = currentMatch.closeMatch();
            /* Se si sta chiudendo, ha senso mettermi in attesa di una risposta */
            if(isClosing) {
                /* Attendo una risposta di conferma di avvenuto inizio da parte del server */
                try {
                    lock.wait();
                } catch (InterruptedException exc) {
                    DebugHelper.log("InterruptedException durante una richiesta di ingresso: " + exc.getMessage());
                }
            }
            return currentMatch == null;
        }
    }
    
    /**
     * Receiver dei messaggi
     * @param msg Messaggio
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        if(msg.getName().equals(Match.MATCH_ACCESS_SUCCESS_NOTIFICATION_MSG)) {
            synchronized(lock) {
                /**
                 * Il giocatore è stato accettato in una partita.
                 */
                try {
                    String matchName = (String) msg.getParameter(0);
                    Player owner = (Player) msg.getParameter(1);
                    // TODO: ricevere regole della partita
                    
                    currentMatch = new RemoteMatch(msg.getSenderConnection(), owner, matchName, new Object());
                    playerJoinedAMatch();
                } catch(ClassCastException ex) {
                    throw new CommunicationException("Wrong parameter type in message " + msg.getName());
                }
            }
        }
    }

    
    /**
     * Richiamato quando il giocatore entra in una partita,
     * aggiorna l'interfaccia ed i listener.
     */
    private void playerJoinedAMatch() {
        P2PConnection c = currentRoom.getConnection();
        /* Listener per ricevere gli aggiornamenti sulla partita */
        c.addMessageReceivedObserver(currentMatch, Match.MATCH_UPDATE_MSG);
        /* Listener per l'inizio della partita */
        c.addMessageReceivedObserver(currentMatch, Match.MATCH_STARTED_MSG);
        /* Listener per la chiusura della partita */
        c.addMessageReceivedObserver(currentMatch, Match.MATCH_CLOSED_MSG);
        /* Listener per la conferma di accesso da parte dell'owner della partita */
        c.removeMessageReceivedObserver(this, Match.MATCH_ACCESS_SUCCESS_NOTIFICATION_MSG);
        /* Aggiornamento interfaccia */
        UnoXTutti.mainWindow.setGuiState(UnoXTuttiGUI.GUIState.INSIDE_MATCH);
    }
    
    
    /**
     * Richiamato quando il giocatore crea una stanza, aggiorna
     * i listener.
     */
    private void playerCreatedAMatch() {
        P2PConnection conn = currentRoom.getConnection();
        conn.addMessageReceivedObserver(currentMatch, Match.MATCH_ACCESS_REQUEST_MSG);
    }
    
    /**
     * Richiamato quando un giocatore partecipante esce/viene fatto uscire dalla partita.
     * Valido sia quando esce di sua volontà, sia quando viene fatto
     * uscire poichè la partita sta chiudendo.
     */
    private void playerLeftAMatch() {
        /* Rimozione dei listener della partita, verrà cancellata */
        P2PConnection conn = currentRoom.getConnection();
        conn.removeMessageReceivedObserver(currentMatch);
        
        /* Rimozione del match */
        currentMatch = null;
        /* Aggiornamento interfaccia grafica, ritorno alla stanza */
        UnoXTutti.mainWindow.setGuiState(UnoXTuttiGUI.GUIState.INSIDE_ROOM);
    }

    /**
     * Metodo chiamato dalla RemoteMatch quando riceve un messaggio
     * di chiusura della stanza, utilizzato dai client 
     * partecipanti (non creatori) della stanza
     */
    public void receivedMatchClosure() {
        synchronized(lock) {
            playerLeftAMatch();
        }
    }
    
    
    /**
     * Richiamato quando una partita viene avviata.
     * Aggiorna l'interfaccia.
     */
    public void matchStarted() {
        UnoXTutti.mainWindow.setGuiState(UnoXTuttiGUI.GUIState.GAMEPLAY);
    }
    
    /**
     * Richiamato quando una partita viene chiusa.
     */
    public void matchClosed() {
        playerLeftAMatch();
        wakeUpController();
    }

}
