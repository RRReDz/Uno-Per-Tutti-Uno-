/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.domain.Player;

/**
 * Questa classe rappresenta una connessione "alla pari". Solo inizialmente
 * serve sapere chi è il Client (colui che richiede la connessione) e chi il
 * Server (colui che la concede) per gestire l'handshake iniziale.
 *
 * @author picardi
 */
public class P2PConnection {

    private class P2PHelper implements Runnable {

        @Override
        public void run() {
            P2PMessage lastMsg = null;
            do {
                try {
                    lastMsg = (P2PMessage) sockIn.readObject();
                    System.out.println("Thread: " + Thread.currentThread() + ", Received Message: " + lastMsg.getName());
                    //System.out.println((serverSide ? "Server: " : "Client: ") + "Received message: " + lastMsg.getName());
                    if (lastMsg.isByeMessage() && !isClosing()) {
                        //System.out.println("Current thread writing message: " + Thread.currentThread().toString());
                        //System.out.println("Current output stream: " + sockOut.toString());
                        try {
                            sendMessage(P2PMessage.createByeMessage());
                        } catch (PartnerShutDownException ex) {
                            // Non fa nulla
                            // Tanto si stava chiudendo
                        }
                        setClosing(true);
                        //Thread.sleep(10000);
                    } else {
                        notifyMessageReceived(lastMsg);
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    //System.out.println("Exception in " + (serverSide ? "Server: " : "Client: "));
                    Logger.getLogger(P2PConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (!isClosing());
            doClose();
        }
    }

    private final Socket mySocket;
    private final boolean serverSide;
    private ObjectInputStream sockIn;
    private ObjectOutputStream sockOut;
    private P2PHelper myHelper;
    private Player player;
    private boolean closed;
    private boolean closing;
    private final HashMap<String, ArrayList<MessageReceiver>> messageReceivers;
    private final HashMap<String, ArrayList<P2PMessage>> unreadMessages;

    private P2PConnection(Socket sock, boolean serverside) {
        mySocket = sock;
        this.serverSide = serverside;
        closed = false;
        messageReceivers = new HashMap<>();
        unreadMessages = new HashMap<>();
    }

    private void doClose() {
        try {
            //System.out.println("Current thread closing: " + Thread.currentThread().toString());
            //System.out.println("Closing output stream: " + sockOut.toString());
            if (serverSide) {
                sockOut.close();
                sockIn.close();
            } else {
                sockIn.close();
                sockOut.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(P2PConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                mySocket.close();
            } catch (IOException ex) {
                Logger.getLogger(P2PConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            setClosed();
        }
    }

    public synchronized void sendMessage(P2PMessage msg) throws PartnerShutDownException {
        try {
            sockOut.writeObject(msg);
        } catch (SocketException ex) {
            throw new PartnerShutDownException("Connection closed");
        } catch (IOException ex) {
            Logger.getLogger(P2PConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void connect() throws IOException, ServerConnectionException {
        if (serverSide) {
            sockIn = new ObjectInputStream(mySocket.getInputStream());
            sockOut = new ObjectOutputStream(mySocket.getOutputStream());
            boolean ok = handshake();
            if (!ok) {
                throw new ServerConnectionException("Could not handshake");
            }
        } else {// clientside 
            sockOut = new ObjectOutputStream(mySocket.getOutputStream());
            sockIn = new ObjectInputStream(mySocket.getInputStream());
            boolean ok = handshake();
            if (!ok) {
                throw new ClientConnectionException("Could not handshake");
            }
        }
        myHelper = new P2PHelper();
        (new Thread(myHelper)).start();
    }

    private boolean handshake() throws IOException {
        boolean ok = true;
        if (serverSide) {
            try {
                P2PMessage helloMsg = (P2PMessage) sockIn.readObject();
                if (helloMsg.isHelloMessage()) {
                    P2PMessage reply = P2PMessage.createHelloMessage();
                    sockOut.writeObject(reply);
                } else {
                    ok = false;
                }
            } catch (ClassNotFoundException ex) {
                ok = false;
            }

            if (!ok) {
                System.out.println("Handshake did not work out.");
                doClose();
            }
        } else {
            try {
                // clientside
                P2PMessage helloMsg = P2PMessage.createHelloMessage();
                sockOut.writeObject(helloMsg);
                P2PMessage rep = (P2PMessage) sockIn.readObject();
                if (!rep.isHelloMessage()) {
                    ok = false;
                }
            } catch (ClassNotFoundException ex) {
                ok = false;
            }
            if (!ok) {
                System.out.println("Handshake did not work out.");
                doClose();
            }
        }
        return ok;
    }

    /**
     * Accept a new connection from a client. In order for the connection to
     * work, the client should create an output and an input stream (in this
     * order).
     *
     * @param serverSock the server Socket
     * @return the P2PConnection with the new client
     * @throws IOException, ServerConnectionException
     */
    public static P2PConnection acceptConnectionRequest(ServerSocket serverSock) throws IOException, ServerConnectionException {
        Socket clientsock = serverSock.accept();
        P2PConnection p2p = new P2PConnection(clientsock, true);
        p2p.connect();
        return p2p;
    }

    /**
     * @param player
     * @param address
     * @param port
     * @return
     * @throws IOException
     */
    public static P2PConnection connectToHost(Player player, InetAddress address, int port) throws IOException {
        Socket clientsock = new Socket(address, port);
        P2PConnection p2p = new P2PConnection(clientsock, false);
        p2p.player = player;
        p2p.connect();
        return p2p;
    }

    /**
     * Restituisce il giocatore associato con questa connessione.
     *
     * @return il Player associato con questa connessione.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Imposta il giocatore associato con questa connessione
     *
     * @param pl il giocatore
     */
    public void setPlayer(Player pl) {
        player = pl;
    }

    /**
     * Chiede alla connessione di chiudersi. Essa manda un Bye message sul
     * canale (vedasi P2PMessage). Ci&ograve; che effettivamente chiude
     * per&ograve; è la ricezione di una risposta al Bye. Se qualcosa va storto
     * la connessione viene chiusa bruscamente richiamando forceClose.
     */
    public synchronized void disconnect() {
        try {
            setClosing(true);
            sockOut.writeObject(P2PMessage.createByeMessage());
        } catch (IOException ex) {
            System.out.println("Could not close down gracefully.");
            forceClose();
        }
    }

    /**
     * Dice se la connessione è chiusa.
     *
     * @return true se la connessione è chiusa, false altrimenti.
     */
    public synchronized boolean isClosed() {
        return closed;
    }

    private synchronized void setClosed() {
        closed = true;
    }

    private synchronized boolean isClosing() {
        return closing;
    }

    private synchronized void setClosing(boolean b) {
        closing = b;
    }

    /**
     * Forza la chiusura brusca e improvvisa della connessione.
     *
     */
    public synchronized void forceClose() {
        if (closed) {
            return;
        }
        doClose();
        closed = true;
    }

    /**
     * Aggiunge un osservatore della classe MessageReceiver che verrà notificato
     * quando arriva un messaggio del tipo richiesto.
     *
     * @param obs l'oggetto che vuole essere notificato dell'arrivo del
     * messaggio
     * @param messageName il nome del messaggio del cui arrivo <em>obs</em>
     * vuole essere notificato.
     */
    public synchronized void addMessageReceivedObserver(MessageReceiver obs, String messageName) {
        ArrayList<MessageReceiver> receivers = this.messageReceivers.get(messageName);
        if (receivers == null) {
            receivers = new ArrayList<>();
            messageReceivers.put(messageName, receivers);
        }
        /* Se la lista non contiene già l'observer, allora lo aggiungo */
        if(!receivers.contains(obs))
            receivers.add(obs);
        
        /* debug */
        System.out.println("\n - addMessageReceivedObserver - ");
        System.out.println("Aggiunto observer " + obs + " per messaggio " + messageName + " su connessione " + this.toString());
        
        ArrayList<P2PMessage> queue = unreadMessages.get(messageName);
        if (queue != null) {
            for (P2PMessage msg : queue) {
                /* debug */
                System.out.println("Messaggio non letto (" + msg + ") per " + obs);
                obs.updateMessageReceived(msg);
            }
            queue.clear();
        }
        /* debug */
        System.out.println();
    }

    /**
     * Rimuove un'istanza della classe MessageReceiver dalla lista degli
     * osservatori dei messaggi con nome <em>messageName</em>. Tale istanza non
     * verrà più; notificata.
     *
     * @param obs l'oggetto che vuole essere rimosso dalla lista degli
     * osservatori
     * @param messageName il nome del messaggio il cui arrivo <em>obs</em> non
     * vuole più; osservare.
     */
    public synchronized void removeMessageReceivedObserver(MessageReceiver obs, String messageName) {
        ArrayList<MessageReceiver> receivers = this.messageReceivers.get(messageName);
        if (receivers == null) {
            return;
        }
        receivers.remove(obs);
    }

    private synchronized void notifyMessageReceived(P2PMessage msg) {
        /* debug */
        System.out.println("\n - notifyMessageReceived - ");
        msg.setSenderConnection(this);
        ArrayList<MessageReceiver> receivers = this.messageReceivers.get(msg.getName());
        if (receivers == null || receivers.isEmpty()) {
            /* debug */
            System.out.println("Il messaggio " + msg + " NON verrà consegnato!\n");
            ArrayList<P2PMessage> queue = unreadMessages.get(msg.getName());
            if (queue == null) {
                queue = new ArrayList<>();
                unreadMessages.put(msg.getName(), queue);
            }
            queue.add(msg);
            return;
        }
        receivers = (ArrayList<MessageReceiver>) receivers.clone();
        /* debug */
        System.out.println("Messaggio in invio ai seguenti observers: ");
        for (MessageReceiver rec : receivers) {
            /* debug */
            System.out.println(rec);
            rec.updateMessageReceived(msg);
        }
        /* debug */
        System.out.println();
    }

    @Override
    public String toString() {
        if(player != null)
            return player.toString();
        else
            return "NULL";
    }
    
}
