/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
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
     * Giocatori della partita
     */
    protected final List<Player> players;
    
    /**
     * Lista dei giocatori che hanno richiesto l'accesso alla partita.
     */
    protected final List<Player> joinRequests;
    
    /**
     * Indica se la partita è stata avviata
     */
    protected boolean started;
    
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
        this.players = new ArrayList<>();
        this.joinRequests = new ArrayList<>();
        players.add(owner);
    }
    
    
    /**
     * Ritorna il creatore della partita
     * @return Proprietario della partita
     */
    public Player getOwner() {
        return owner;
    }
    
    
    /**
     * Set del parametro relativo all'avvio della partita
     * @param start
     */
    public void setStarted(boolean start) {
        started = start;
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
     * Ritorna i giocatori della partita
     * @return Lista dei giocatori presenti nella partita
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * Metodo per notificare a tutti i giocatori in stanza l'inzio della partita.
     */
    void notifyMatchStart(P2PConnection sender) {
        /* 
         * Qui ci andrà un messaggio di tipo Match.MATCH_STARTED_MSG
         * Ricordarsi anche di registrare l'observer quando si accede alla partita
         */
        P2PMessage upd = new P2PMessage(Match.MATCH_UPDATE_MSG);
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
        return !(players.contains(player) || joinRequests.contains(player) || player.equals(owner));
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
            conn.disconnect();
            room.removePlayer(conn);
        }
        return success;
    }
    
    
    /**
     * Listener per messaggi
     * @param msg Messaggio
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        /* Messaggio di risposta del proprietario ad una richiesta di accesso */
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
            Player matchOwner = msg.getSenderConnection().getPlayer();
            Player applicant = (Player) msg.getParameter(0);
            boolean accepted = (boolean) msg.getParameter(1);
            
            /* Controllo validità messaggio */
            if(!this.owner.equals(matchOwner)) {
                throw new IllegalStateException("Il giocatore non è il proprietario della partita.");
            }
            
            synchronized(room) {
                if(!joinRequests.contains(applicant)) {
                    /**
                     * Il giocatore non è nella lista delle richieste: potrebbe essere
                     * stato rimosso da essa in quanto è entrato in un'altra partita.
                     */
                    return;
                }
                
                if(accepted && !room.playerIsInAMatch(applicant)) {
                    if(tellPlayerToJoin(applicant)) {
                        addPlayer(applicant);
                    }
                    room.deleteAccessRequests(applicant);
                }
                
                /* Richiesta gestita, pulizia */
                joinRequests.remove(applicant);
                if(joinRequests.isEmpty()) {
                    /* Non ci sono più richieste da gestire, si rimuove il listener */
                    msg.getSenderConnection().removeMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
                }
            }
        } catch (ClassCastException ex) {
            throw new CommunicationException("Wrong parameter type in message " + msg.getName());
        }
    }
    
    
    /**
     * Aggiunge un giocatore alla partita.
     * @param player Giocatore da aggiungere
     */
    protected void addPlayer(Player player) {
        if(players.contains(player)) {
            throw new IllegalStateException("Il giocatore è già presente nella partita.");
        }
        players.add(player);
        sendMatchUpdate();
    }
    
    
    /**
     * Rimuove un giocatore dalla partita
     * @param player Giocatore da rimuovere
     */
    protected void removePlayer(Player player) {
        if(!players.contains(player)) {
            throw new IllegalStateException("Il giocatore non è presente nella parita.");
        }
        players.remove(player);
        sendMatchUpdate();
    }
    
    
    /**
     * Manda un messaggio di aggiornamento a tutti i giocatori
     */
    void sendMatchUpdate() {
        synchronized(room) {
            /* Si manda un messaggio di aggiornamento a tutti i giocatori */
            P2PMessage upd = new P2PMessage(Match.MATCH_UPDATE_MSG);
            Object[] parameters = new Object[]{ this.getPlayers() };
            upd.setParameters(parameters);
            List<P2PConnection> lostConnections = new ArrayList<>(players.size());
            
            /* Si aggiorna ogni giocatore */
            players.stream().map((p) -> room.getConnectionWithPlayer(p)).forEachOrdered((c) -> {
                try {
                    c.sendMessage(upd);
                } catch (PartnerShutDownException ex) {
                    lostConnections.add(c);
                }
            });
            
            /* Chiusura connessioni morte */
            lostConnections.stream().map((c) -> {
                c.disconnect();
                return c;
            }).forEachOrdered((c) -> {
                room.removePlayer(c);
            });
            
            /* Rimozione giocatori disconnessi e, in tal caso, ri-aggiornamento */
            lostConnections.forEach((c) -> {
                players.remove(c.getPlayer());
            });
            if(lostConnections.size() > 0) {
                sendMatchUpdate();
                room.sendRoomUpdate();
            }
        }
    }
    
    
    /**
     * Indica se un determinato giocatore è presente nella partita
     * @param player Giocatore da cercare
     * @return <code>true</code> se il giocatore si trova nella partita,
     *          <code>false</code> altrimenti.
     */
    protected boolean containsPlayer(Player player) {
        return players.contains(player);
    }

    /**
     * Rimuove un giocatore dalla lista delle richieste
     * @param player Giocatore da rimuovere
     */
    void removePlayerAccessRequest(Player player) {
        joinRequests.remove(player);
        if(joinRequests.isEmpty()) {
            /* Inutile ascoltare messaggi se non ci sono richieste valide */
            room.getConnectionWithPlayer(owner)
                    .removeMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
        }
    }

    /**
     * Informa il giocatore richiedente che la sua richiesta di accesso
     * ad una partita è stata accettata.
     * 
     * Il metodo sposta dunque il giocatoe all'interno della partita.
     * @param player Giocatore
     * @return <code>true</code> se il messaggio viene inviato con successo,
     *          <code>false</code> altrimenti.
     */
    private boolean tellPlayerToJoin(Player player) {
        P2PConnection conn = room.getConnectionWithPlayer(player);
        P2PMessage message = new P2PMessage(Match.MATCH_ACCESS_SUCCESS_NOTIFICATION_MSG);
        message.setParameters(new Object[] { matchName, owner });
        // TODO: passare opzioni
        try {
            conn.sendMessage(message);
            return true;
        } catch (PartnerShutDownException ex) {
            conn.disconnect();
            room.removePlayer(conn);
            Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
