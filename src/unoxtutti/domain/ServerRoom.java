/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;
import unoxtutti.connection.ServerCreationException;
import unoxtutti.utils.DebugHelper;

/**
 * La classe ServerRoom rappresenta una Room (Stanza) lato Server La Room lato
 * Server è il Server stesso.
 *
 * @author picardi
 */
public class ServerRoom extends Room implements Runnable, MessageReceiver {

    private final Player owner;
    private ServerSocket serverSock;
    private boolean shouldClose;
    private boolean closed;
    private final Object closeDownMonitor;

    private final HashMap<Player, P2PConnection> connections;
    private final ArrayList<P2PConnection> waitingClients;
    
    /**
     * Partite all'interno della stanza. La chiave utilizzata
     * dalla mappa è il nome della partita.
     */
    private final HashMap<String, ServerMatch> matches;

    /**
     * Il costruttore è privato; una ServerRoom può essere creata solo tramite
     * il factory method
     * <em>createServerRoom</em>
     *
     * @param p Il giocatore che crea la stanza
     * @param roomName Il nome della stanza con cui gli altri giocatori potranno
     * connettersi
     */
    private ServerRoom(Player p, String roomName) {
        super(roomName);
        owner = p;
        closed = false;
        closeDownMonitor = new Object();
        connections = new HashMap<>();
        waitingClients = new ArrayList<>();
        matches = new HashMap<>();
    }

    /**
     * Permette a un thread di aspettare per un certo tempo che il server si
     * chiuda.
     *
     * @param timeout Il tempo per cui aspettare.
     * @throws InterruptedException
     */
    public void waitOnClose(long timeout) throws InterruptedException {
        synchronized (closeDownMonitor) {
            if ((closeDownMonitor != null) && (!isClosed())) {
                closeDownMonitor.wait(timeout);
            }
        }
    }

    /**
     * Indica al Server che dovrebbe iniziare le procedure di chiusura
     */
    public void shouldClose() {
        boolean ok = false;
        synchronized (closeDownMonitor) {
            shouldClose = true;
            ok = isClosed();
        }
        // Sveglia se stesso dall'attesa di connessioni
        // stabilendo una P2PConnection con se stesso
        if (ok) {
            return;
        }

        P2PConnection conn = null;
        try {
            conn = P2PConnection.connectToHost(owner, this.getAddress(), this.getPort());
        } catch (IOException ex) {
            Logger.getLogger(ServerRoom.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void justStarted() {
        synchronized (closeDownMonitor) {
            shouldClose = false;
        }
    }

    private boolean shouldIClose() {
        synchronized (closeDownMonitor) {
            return shouldClose;
        }
    }

    private void setClosed(boolean b) {
        synchronized (closeDownMonitor) {
            boolean prev = isClosed();
            closed = b;
            if (!prev) {
                closeDownMonitor.notifyAll();
            }
        }
    }

    /**
     * Permette di verificare se il Server si è effettivamente chiuso.
     *
     * @return true se il Server è chiuso, false altrimenti.
     */
    public boolean isClosed() {
        boolean ret = false;
        synchronized (closeDownMonitor) {
            ret = closed;
        }
        return ret;
    }

    /**
     * Forza lo shut down del Server interrompendo tutte le connessioni ai
     * client.
     */
    public void forceClose() {
        synchronized (closeDownMonitor) {
            if (isClosed()) {
                return;
            }
        }
        for (P2PConnection p2p : connections.values()) {
            if (!p2p.isClosed()) {
                p2p.forceClose();
            }
        }
        setClosed(true);
    }

    /**
     * Crea una nuova ServerRoom e fa partire il Server.
     *
     * @param p il Giocatore che crea la Room
     * @param roomName il nome della Room
     * @param port la porta su cui aprire la Room
     * @return la ServerRoom appena creata
     * @throws ServerCreationException se non riesce ad ottenere l'indirizzo di
     * "localhost"
     */
    public static ServerRoom createServerRoom(Player p, String roomName, int port) throws ServerCreationException {
        ServerRoom room = new ServerRoom(p, roomName);
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException exc) {
            throw new ServerCreationException("Cannot find localhost. Server creation impossible.");
        }
        room.setAddress(localhost);
        room.setPort(port);
        (new Thread(room)).start();
        return room;
    }

    @Override
    /**
     * L'esecuzione vera e propria del Server thread
     *
     */
    public void run() {
        if (closed) {
            return;
        }
        try {
            serverSock = new ServerSocket(getPort());
            justStarted();

            while (!shouldIClose()) {
                P2PConnection playerConnection = P2PConnection.acceptConnectionRequest(serverSock);
                synchronized (waitingClients) {
                    waitingClients.add(playerConnection);
                    playerConnection.addMessageReceivedObserver(this, Room.ROOM_ENTRANCE_REQUEST_MSG);
                    playerConnection.addMessageReceivedObserver(this, Match.MATCH_CREATION_REQUEST_MSG);
                }
            }
            System.out.println("Server is closing down");
            ArrayList<P2PConnection> disc = new ArrayList<>();
            disc.addAll(connections.values());
            disc.addAll(waitingClients);
            for (P2PConnection p2p : disc) {
                p2p.disconnect();
            }
            boolean canClose = false;
            while (!canClose) {
                canClose = true;
                for (P2PConnection p2p : connections.values()) {
                    if (!p2p.isClosed()) {
                        canClose = false;
                    }
                }
            }
            System.out.println("All helpers stopped");
            setClosed(true);
            serverSock.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerRoom.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void addPlayer(P2PConnection conn) {
        this.connections.put(conn.getPlayer(), conn);
    }

    protected void removePlayer(P2PConnection conn) {
        this.connections.remove(conn.getPlayer());
    }

    /**
     * Restituisce il numero di giocatori presenti nella stanza.
     *
     * @return il numero di giocatori presenti nella stanza
     */
    @Override
    public int getPlayerCount() {
        return connections.keySet().size();
    }

    /**
     * Restituisce una copia dell'elenco di giocatori presenti nella stanza.
     *
     * @return l'elenco (in copia) dei giocatori presenti nella stanza
     */
    @Override
    public ArrayList<Player> getPlayers() {
        ArrayList<Player> ret = new ArrayList<>();
        ret.addAll(connections.keySet());
        return ret;
    }

    @Override
    public void updateMessageReceived(P2PMessage msg) {
        String msgName = msg.getName();
        switch(msgName) {
            case Room.ROOM_ENTRANCE_REQUEST_MSG:
                DebugHelper.log("ROOM: ricevuta richiesta di ingresso.");
                handleEntranceRequest(msg);
                break;
            case Room.ROOM_EXIT_MSG:
                DebugHelper.log("ROOM: ricevuta notifica di uscita.");
                handleExit(msg);
                break;
            case Match.MATCH_CREATION_REQUEST_MSG:
                DebugHelper.log("ROOM: ricevuta richiesta di creazione partita.");
                handleMatchCreation(msg);
                break;
        }
    }

    private void handleEntranceRequest(P2PMessage msg) {
        boolean reqOk = true;
        Player player = null;
        if (msg.getParametersCount() != 2) {
            reqOk = false;
        } else {
            try {
                String roomName = (String) msg.getParameter(0);
                player = (Player) msg.getParameter(1);
                if (!roomName.equals(this.getName())) {
                    reqOk = false;
                }
            } catch (ClassCastException ex) {
                reqOk = false;
            }
        }
        P2PConnection sender = msg.getSenderConnection();
        P2PMessage reply = new P2PMessage(Room.ROOM_ENTRANCE_REPLY_MSG);
        Object[] parameters = new Object[2]; // reply + players
        reply.setParameters(parameters);
        parameters[0] = reqOk;

        synchronized (this) {
            if (reqOk && player != null) {
                sender.setPlayer(player);
                addPlayer(sender);
                waitingClients.remove(sender);
                parameters[1] = this.getPlayers();
                sender.addMessageReceivedObserver(this, Room.ROOM_EXIT_MSG);
                sender.removeMessageReceivedObserver(this, Room.ROOM_ENTRANCE_REQUEST_MSG);
                try {
                    sender.sendMessage(reply);
                    sendRoomUpdate();
                } catch (PartnerShutDownException ex) {
                    sender.disconnect();
                    removePlayer(sender);
                }
            }
            this.waitingClients.remove(sender);
        }
    }

    private void handleExit(P2PMessage msg) {
        synchronized (this) {
            this.removePlayer(msg.getSenderConnection());
            sendRoomUpdate();
        }
    }

    private void sendRoomUpdate() {
        P2PMessage upd = new P2PMessage(Room.ROOM_UPDATE_MSG);
        Object[] updpar = new Object[]{this.getPlayers(), this.getAvailableMatches()};
        upd.setParameters(updpar);
        while (upd != null) {
            ArrayList<P2PConnection> unresp = new ArrayList<>();
            for (P2PConnection client : connections.values()) {
                try {
                    client.sendMessage(upd);
                } catch (PartnerShutDownException ex) {
                    unresp.add(client);
                }
            }
            for (P2PConnection p2p : unresp) {
                p2p.disconnect();
                removePlayer(p2p);
            }
            if (unresp.size() > 0) {
                upd.setParameters(new Object[]{this.getPlayers()});
            } else {
                upd = null;
            }
        }
    }
    
    /**
     * Gestisce la richiesta di creazione di una partita.
     * @param msg Messaggio di richiesta
     */
    private void handleMatchCreation(P2PMessage msg) {
        /* Controllo validità dati ricevuti */
        boolean reqOk = true;
        Player matchOwner = null;
        String matchName = null;
        if (msg.getParametersCount() != 2) {
            reqOk = false;
        } else {
            try {
                matchOwner = (Player) msg.getParameter(0);
                matchName = (String) msg.getParameter(1);
                if(matches.containsKey(matchName)) {
                    /* Esiste già una partita con lo stesso nome */
                    reqOk = false;
                }
            } catch (ClassCastException ex) {
                reqOk = false;
            }
        }
        
        /* Costruzione messaggio di risposta */
        P2PConnection sender = msg.getSenderConnection();
        P2PMessage reply = new P2PMessage(Match.MATCH_CREATION_REPLY_MSG);
        Object[] parameters = new Object[1];
        reply.setParameters(parameters);
        parameters[0] = reqOk;
        
        /* Creazione della partita ed invio risposta */
        synchronized(this) {
            if(reqOk && matchOwner != null) {
                /* Creazione della partia */
                createMatch(matchOwner, matchName);
                sender.addMessageReceivedObserver(this, Match.MATCH_DESTROY_MSG);
                sender.removeMessageReceivedObserver(this, Match.MATCH_CREATION_REQUEST_MSG);
                //waitingClients.remove(sender);    ???
            }
            
            /* Invio risposta (sia in caso di successo che insuccesso) */
            try {
                sender.sendMessage(reply);
                sendRoomUpdate();
            } catch (PartnerShutDownException ex) {
                sender.disconnect();
                removePlayer(sender);
            }
            //waitingClients.remove(sender); ???
        }
    }
    
    /**
     * Inizializza una partita e la aggiunge alla lista delle partite.
     * @param matchOwner Proprietario della partita
     * @param matchName Nome della partita
     */
    private void createMatch(Player matchOwner, String matchName) {
        if(matchOwner == null || matchName == null || matchName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Dati mancanti per la creazione di una partita."
            );
        }
        matches.put(
                matchName,
                new ServerMatch(
                    matchOwner,
                    matchName,
                    new Object() // TODO: Opzioni
                )
        );
    }
    
    /**
     * Ritorna la lista delle partite.
     * @return Lista delle partite.
     */
    @Override
    public ArrayList<String> getAvailableMatches() {
        ArrayList<String> matchesList = new ArrayList<>();
        matches.values().stream().filter((m) -> (!m.isStarted())).forEachOrdered((m) -> {
            matchesList.add(m.getMatchName());
        });
        
        /* Per i plebei: */
        //for(ServerMatch m : matches.values()) {
        //    if(!m.isStarted()) {
        //        matchesList.add(m.getMatchName());
        //    }
        //}
        
        return matchesList;
    }
}
