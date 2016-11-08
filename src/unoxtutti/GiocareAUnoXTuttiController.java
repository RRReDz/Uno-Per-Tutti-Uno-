/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import unoxtutti.connection.ServerCreationException;
import unoxtutti.domain.Player;
import unoxtutti.domain.RemoteRoom;
import unoxtutti.domain.ServerRoom;

/**
 * Controller GRASP per l'UC "GiocareAUnoXTutti". È un singleton, quindi l'unica
 * istanza di questa classe viene ottenuta tramite il metodo statico getInstance
 *
 * @author picardi
 */
public class GiocareAUnoXTuttiController {

    private static GiocareAUnoXTuttiController singleInstance;

    private final HashMap<String, ServerRoom> serverRooms;
    private RemoteRoom currentRoom;
    private RemoteRoom roomInLimbo;
    private final Object entranceWaiting;
    private final Player player;

    // GUI utility objects
    // nomi stanze ordinati alfabeticamente
    private final DefaultListModel<String> serverRoomNames;

    private GiocareAUnoXTuttiController(Player pl) {
        player = pl;
        serverRooms = new HashMap<>();
        serverRoomNames = new DefaultListModel<>();
        entranceWaiting = new Object();
    }

    /**
     *
     * @return il giocatore che sta interagendo con l'applicazione. Tale
     * giocatroe deve essere definito perch&eacute; il caso d'uso possa iniziare
     * e il controller abbia dunque senso di esistere.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Crea un'istanza del controller GRASP dedicata al giocatore rappresentato
     * da Player. Corrisponde alla precondizione dell'UC che il giocatore sia
     * autenticato (se non lo fosse, non esisterebbe alcun Player).
     *
     * @param aPlayer il giocatore che sta interagendo con l'applicazione.
     */
    public static GiocareAUnoXTuttiController getInstance(Player aPlayer) {
        if (singleInstance == null || singleInstance.player != aPlayer) {
            singleInstance = new GiocareAUnoXTuttiController(aPlayer);
        }
        return singleInstance;
    }

    /**
     * Operazione utente definita nei contratti
     *
     * @param roomName il nome della stanza (server) da aprire
     * @param port la porta su cui aprire il server
     * @return un oggetto ServerRoom che rappresenta il server creato
     * @throws ServerCreationException
     */
    public ServerRoom apriStanza(String roomName, int port) throws ServerCreationException {
        ServerRoom room = ServerRoom.createServerRoom(player, roomName, port);
        addServerRoom(room);
        return room;
    }

    private void addServerRoom(ServerRoom room) {
        serverRooms.put(room.getName(), room);
        ArrayList<String> names = new ArrayList<>();
        names.addAll(serverRooms.keySet());
        Collections.sort(names);
        serverRoomNames.clear();
        for (String n : names) {
            serverRoomNames.addElement(n);
        }
    }

    private void removeServerRoom(ServerRoom room) {
        serverRooms.remove(room.getName());
        serverRoomNames.removeElement(room.getName());
    }

    /**
     * Operazione utente definita nei contratti
     *
     * @param aRoom la stanza (server) da chiudere
     * @return true se la chiusura è andata a buon fine, false altrimenti.
     */
    public boolean chiudiStanza(ServerRoom aRoom) {
        aRoom.shouldClose();
        try {
            aRoom.waitOnClose(3000);
        } catch (InterruptedException ex) {

        } finally {
            if (!aRoom.isClosed()) {
                aRoom.forceClose();
            }
        }
        removeServerRoom(aRoom);
        return true;
    }

    /**
     *
     * @return fornisce un ListModel contenente i nomi delle stanze esistenti,
     * per poterli visualizzare in modo che restino aggiornati quando l'elenco
     * cambia.
     */
    public ListModel<String> getServerRoomNames() {
        return serverRoomNames;
    }

    /**
     * Fornisce la stanza con un certo nome (se esiste).
     *
     * @param rname Il nome della stanza desiderata
     * @return La stanza desiderata, null se il nome non corrisponde ad alcuna
     * stanza.
     */
    public ServerRoom getRoom(String rname) {
        return serverRooms.get(rname);
    }

    /**
     * Operazione utente definita nei contratti. Il thread viene sospeso sinchè
     * l'ingresso nella stanza non è; avvenuto.
     *
     * @param roomName Il nome della stanza in cui entrare
     * @param roomAddr L'indirizzo della stanza in cui entrare
     * @param roomPort La porta della stanza in cui entrare
     */
    public void entraInStanza(String roomName, String roomAddr, int roomPort) {
        synchronized (entranceWaiting) {
            roomInLimbo = RemoteRoom.createRemoteRoom(this.player, roomName, roomAddr, roomPort);
            if (roomInLimbo != null) {
                try {
                    entranceWaiting.wait();
                } catch (InterruptedException ex) {
                    roomInLimbo = null;
                }
            }
            if (roomInLimbo != null) {
                currentRoom = roomInLimbo;
                roomInLimbo = null;
            } else {
                currentRoom = null;
            }
        }
    }

    /**
     *
     * @return la stanza in cui il giocatore si trova attualmente, se ne esiste
     * una. null altrimenti.
     */
    public RemoteRoom getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Metodo invocato dalle classi del "model" quando il server comunica che
     * l'ingresso nella stanza è, completato (si tratta di un evento asincrono).
     * Sveglia il thread che era stato messo in attesa durante "entraInRoom".
     *
     * @param room La stanza in cui si è entrati.
     */
    public void roomEntranceCompleted(RemoteRoom room) {
        synchronized (entranceWaiting) {
            roomInLimbo = room;
            entranceWaiting.notifyAll();
        }
    }

    /**
     * Metodo invocato dalle classi del "model" quando il server comunica che
     * l'ingresso nella stanza è fallito (si tratta di un evento asincrono).
     * Sveglia il thread che era stato messo in attesa durante "entraInRoom".
     *
     * @param room La stanza in cui non si è riusciti ad entrare.
     */
    public void roomEntranceFailed(RemoteRoom room) {
        synchronized (entranceWaiting) {
            roomInLimbo = null;
            entranceWaiting.notifyAll();
        }
    }

    /**
     * Metodo invocato dalle classi del "model" quando il server comunica che le
     * informazioni relative alla stanza sono cambiate.
     */
    public void roomUpdated() {
        /* Per ora non deve fare nulla: la lista di Player
		si dovrebbe aggiornare da sola, trattandosi di un
		DefaultListModel.
         */
    }

    /**
     * Operazione utente definita nei contratti.
     */
    public void esciDalGioco() {
        if (currentRoom != null) {
            this.esciDaStanza();
        }
        ArrayList<ServerRoom> toclose = new ArrayList<>();
        toclose.addAll(serverRooms.values());
        for (ServerRoom r : toclose) {
            this.chiudiStanza(r);
        }
    }

    /**
     * Operazione utente definita nei contratti.
     */
    public void esciDaStanza() {
        if (currentRoom != null) {
            currentRoom.exit();
            currentRoom = null;
        }
    }

    public boolean inStanza() {
        return (currentRoom != null);
    }
}
