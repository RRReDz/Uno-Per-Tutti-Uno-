/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
 
package unoxtutti; 
 
import unoxtutti.domain.RemoteRoom; 
 
/** 
 *  
 * @author Riccardo Rossi 
 */ 
public class GiocarePartitaController { 
     
    private static GiocarePartitaController singleInstance; 
    private RemoteRoom remoteRoom; 
     
    private GiocarePartitaController(RemoteRoom remoteRoom) { 
        this.remoteRoom = remoteRoom; 
    } 
     
    /** 
     * Pattern Singleton 
     */ 
     
    public static GiocarePartitaController getInstance(RemoteRoom remoteRoom) { 
        if(singleInstance == null || singleInstance.remoteRoom != remoteRoom) 
            singleInstance = new GiocarePartitaController(remoteRoom); 
        return singleInstance; 
    } 
     
    /** 
     *  
     * @param nomePartita 
     * @param opzioni -> Non sarà una stringa  
     */ 
    public void creaPartita(String nomePartita, String opzioni) { 
        remoteRoom.hostRemoteMatch(nomePartita, opzioni); 
    } 
} 
