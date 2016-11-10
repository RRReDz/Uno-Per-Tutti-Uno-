/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
 
package unoxtutti; 
 
import unoxtutti.domain.RemoteRoom; 
 
/** 
 * Controller singleton
 * @author Riccardo Rossi 
 */ 
public class GiocarePartitaController {
    private static GiocarePartitaController singleInstance;
    private RemoteRoom remoteRoom;
    
    /**
     * Non permette di generare oggetti della classe
     * al di fuori del metodo getInstance()
     */
    private GiocarePartitaController() { 
    }
    
    /**
     * Ritorna l'istanza del controller di una determinata stanza.
     * Se il controller attualmente memorizzato è di un'altra stanza,
     * viene abbandonato e sostituito da un altro controller.
     * @param remoteRoom Stanza di cui si vuole il controller
     * @return Istanza di <code>GiocarePartitaController</code>
     */
    public static GiocarePartitaController getInstance() { 
        if(singleInstance == null) 
            singleInstance = new GiocarePartitaController(); 
        return singleInstance; 
    }
    
    /**
     * Imposta la stanza remota
     * @param newRoom 
     */
    public void setRemoteRoom(RemoteRoom newRoom) {
        remoteRoom = newRoom;
    }
     
    /** 
     * Crea una partita
     * @param nomePartita 
     * @param opzioni
     */ 
    public void creaPartita(String nomePartita, Object opzioni) { 
        remoteRoom.hostRemoteMatch(nomePartita, opzioni); 
    }
} 
