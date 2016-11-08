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
    private final RemoteRoom remoteRoom;
    
    private GiocarePartitaController(RemoteRoom remoteRoom) { 
        this.remoteRoom = remoteRoom; 
    }
    
    /**
     * Ritorna l'istanza del controller di una determinata stanza.
     * Se il controller attualmente memorizzato è di un'altra stanza,
     * viene abbandonato e sostituito da un altro controller.
     * @param remoteRoom Stanza di cui si vuole il controller
     * @return Istanza di <code>GiocarePartitaController</code>
     */
    public static GiocarePartitaController getInstance(RemoteRoom remoteRoom) { 
        if(singleInstance == null || singleInstance.remoteRoom != remoteRoom) 
            singleInstance = new GiocarePartitaController(remoteRoom); 
        return singleInstance; 
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
