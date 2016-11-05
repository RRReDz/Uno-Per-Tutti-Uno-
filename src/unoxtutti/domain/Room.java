/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * La classe Room è una rappresentazione astratta di una Stanza del gioco. Oltre
 * ad essere caratterizzata da nome, indirizzo e porta, permette di conoscere i
 * giocatori che la popolano. Dovrà essere eventualmente estesa per mostrare
 * anche altre informazioni, come ad esempio le partite che contiene.
 *
 * @author picardi
 */
public abstract class Room {

    public static final String roomEntranceRequestMsg = "roomEntranceRequest";
    public static final String roomEntranceReplyMsg = "roomEntranceReply";
    public static final String roomUpdateMsg = "roomUpdate";
    public static final String roomExitMsg = "roomExit";

    protected final String name;
    protected InetAddress address;
    protected int port;

    /**
     * Crea una nuova Room con nome, indirizzo e porta specificati.
     *
     * @param name il nome della stanza
     * @param address l'indirizzo della stanza
     * @param port la porta della stanza
     */
    protected Room(String name, InetAddress address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    /**
     * Crea una nuova Room senza immediatamente specificare indirizzo e porta.
     *
     * @param name il nome della stanza
     */
    protected Room(String name) {
        this.name = name;
    }

    /**
     * @return il nome della stanza
     */
    public String getName() {
        return name;
    }

    /**
     * @return l'indirizzo della stanza
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @return la porta della stanza
     */
    public int getPort() {
        return port;
    }

    /**
     * Imposta l'indirizzo della stanza
     *
     * @param address l'indirizzo
     */
    protected void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Imposta la porta della stanza
     *
     * @param port la porta
     */
    protected void setPort(int port) {
        this.port = port;
    }

    /**
     * Restituisce il numero di giocatori presenti nella stanza.
     *
     * @return il numero di giocatori presenti nella stanza
     */
    public abstract int getPlayerCount();

    /**
     * Restituisce una copia dell'elenco di giocatori presenti nella stanza.
     *
     * @return l'elenco (in copia) dei giocatori presenti nella stanza
     */
    public abstract ArrayList<Player> getPlayers();

    /**
     * Restituisce una descrizione testuale della stanza.
     *
     * @return una descrizione testuale della stanza.
     */
    public String getInfo() {
        return "Nome: " + name + "\n"
                + "Indirizzo: " + address.getHostAddress() + "\n"
                + "Porta: " + port + "\n"
                + "Numero Giocatori: " + getPlayerCount();
    }
}
