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
    /**
     * Istanza del controller
     */
    private static GiocarePartitaController instance;
    
    /**
     * La stanza di appartenenza, viene settata in automatico da
     * <code>GiocareAUnoXTuttiController</code> durante l'accesso e l'uscita.
     * 
     * Quando non si è in una stanza, questo valore è <code>null</code>.
     */
    private RemoteRoom remoteRoom;
    
    /**
     * Non permette di generare oggetti della classe
     * al di fuori del metodo getInstance()
     */
    private GiocarePartitaController() {
    }
    
    /**
     * Ritorna l'istanza del controller.
     * @return Istanza di <code>GiocarePartitaController</code>
     */
    public static GiocarePartitaController getInstance() {
        if(instance == null) {
            instance = new GiocarePartitaController();
        }
        return instance;
    }
    
    /**
     * Imposta la stanza remota
     * @param newRoom 
     */
    protected void setRemoteRoom(RemoteRoom newRoom) {
        remoteRoom = newRoom;
    }
    
    /** 
     * Crea una partita
     * @param nomePartita Nome della partita
     * @param opzioni
     */ 
    public void creaPartita(String nomePartita, Object opzioni) {
        remoteRoom.createRemoteMatch(nomePartita, opzioni);
        // TODO: Cambiare nome in remoteRoom.createRemoteMatch() per chiarezza
    }
}
