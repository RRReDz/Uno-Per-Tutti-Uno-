/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.connection.CommunicationException;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;

/**
 * Rappresenta una partita lato server.
 * 
 * @author Davide
 */
public class ServerMatch extends Match implements MessageReceiver {
    /**
     * Stanza di appartenenza
     */
    protected final ServerRoom room;
    
    /**
     * Proprietario della partita
     */
    protected final Player owner;
    
    /**
     * Indica se la partita è stata avviata
     */
    protected boolean started;
    
    /**
     * Lista dei giocatori che hanno richiesto l'accesso alla partita.
     */
    protected final List<Player> joinRequests;
    
    /**
     * Inizializza una partita
     * @param parentRoom Stanza di appartenenza
     * @param owner Proprietario della partita
     * @param name Nome
     * @param options Opzioni
     */
    public ServerMatch(ServerRoom parentRoom, Player owner, String name, Object options) {
        super(name, options);
        this.room = parentRoom;
        this.owner = owner;
        this.started = false;
        this.joinRequests = new LinkedList<>();
    }
    
    
    /**
     * Ritorna il creatore della partita
     * @return Proprietario della partita
     */
    public Player getOwner() {
        return owner;
    }
    
    
    /**
     * Indica se la partita è stata avviata oppure no.
     * @return <code>true</code> se la partita è stata avviata,
     *          <code>false</code> se questa è ancora disponibile.
     */
    public boolean isStarted() {
        return started;
    }
    
    /**
     * Metodo per notificare a tutti i giocatori in stanza l'inzio della partita.
     */
    void notifyMatchStart(P2PConnection sender) {
        P2PMessage upd = new P2PMessage(Match.MATCH_STARTED_MSG);
        /**
         * TODO: Inviare i messaggi alla lista dei giocatori in partita.
         * for (P2PConnection client : connections.values())
         *     client.sendMessage(upd);
         */
        
        /* Test di risposta all'owner */
        try {
            sender.sendMessage(upd);
        } catch (PartnerShutDownException exc) {
            Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, exc);
        }
    }
    
    /**
     * Controlla se un determinato giocatore potrebbe entrare nella partita.
     * @param player Giocatore che desidera effettuare l'accesso
     * @return <code>true</code> se tutto va bene, <code>false</code> altrimenti.
     */
    public boolean canPlayerJoin(Player player) {
        return !(joinRequests.contains(player) || player.equals(owner));
    }
    
    
    /**
     * Chiede al proprietario della partita se un determinato giocatore
     * può entrare.
     * @param player Giocatore che desidera effettuare l'accesso.
     * @return <code>true</code> se la richiesta è stata inoltrata con successo,
     *          <code>false</code> altrimenti.
     */
    public boolean askOwnerIfPlayerCanJoin(Player player) {
        /* Abbiamo già fatto questo controllo, ma lo rifacciamo. */
        if(!canPlayerJoin(player)) return false;
        
        /**
         * Dato che questo metodo è richiamato da RoomServer, e RoomServer non
         * ha tempo da perdere, si comunica al giocatore la richiesta e poi
         * non si rimane in attesa di una risposta (dato che non è istantanea).
         */
        
        /* Preparazione del messaggio */
        P2PMessage msg = new P2PMessage(Match.MATCH_ACCESS_REQUEST_MSG);
        Object[] pars = new Object[]{player};
        msg.setParameters(pars);
        P2PConnection conn = room.getConnectionWithPlayer(owner);
        
        /* Invio del messaggio al proprietario */
        boolean success = true;
        try {
            conn.sendMessage(msg);
            joinRequests.add(player);
            if(joinRequests.size() == 1) {
                conn.addMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
            }
        } catch (PartnerShutDownException ex) {
            success = false;
            // TODO: Comunicare alla stanza che il proprietario della partita è morto
        }
        return success;
    }
    
    
    /**
     * Listener per messaggi
     * @param msg Messaggio
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        if(msg.getName().equals(Match.MATCH_ACCESS_REQUEST_REPLY_MSG)) {
            handleMatchAccessAnswer(msg);
        }
    }
    
    
    /**
     * Il proprietario della partita ha comunicato il proprio giudizio su 
     * un giocatore.
     * @param msg Messaggio
     */
    private void handleMatchAccessAnswer(P2PMessage msg) {
        try {
            if(msg.getParametersCount() != 2) return;
            Player applicant = (Player) msg.getParameter(0);
            boolean accepted = (boolean) msg.getParameter(1);
            
            
            
            /* Richiesta gestita, pulizia */
            joinRequests.remove(applicant);
            if(joinRequests.isEmpty()) {
                /* Non ci sono più richieste da gestire, si rimuove il listener */
                msg.getSenderConnection().removeMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
            }
        } catch (ClassCastException ex) {
            throw new CommunicationException("Wrong parameter type in message " + msg.getName());
        }
    }
    
}
