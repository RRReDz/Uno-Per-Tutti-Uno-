/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.LinkedList;
import java.util.List;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;

/**
 * Rappresenta una partita lato server.
 * 
 * @author Davide
 */
public class ServerMatch extends Match {
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
     * Controlla se un determinato giocatore potrebbe entrare nella partita.
     * @param player Giocatore che desidera effettuare l'accesso
     * @return <code>true</code> se tutto va bene, <code>false</code> altrimenti.
     */
    public boolean canPlayerJoin(Player player) {
        return true;
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
        joinRequests.add(player);
        
        /* Preparazione del messaggio */
        P2PMessage msg = new P2PMessage(Match.MATCH_ACCESS_REQUEST_MSG);
        Object[] pars = new Object[]{player};
        msg.setParameters(pars);
        P2PConnection conn = room.getConnectionWithPlayer(owner);
        
        /* Invio del messaggio al proprietario */
        boolean success = true;
        try {
            conn.sendMessage(msg);
        } catch (PartnerShutDownException ex) {
            success = false;
            // TODO: Comunicare alla stanza che il proprietario della partita è morto
        }
        return success;
    }
}
