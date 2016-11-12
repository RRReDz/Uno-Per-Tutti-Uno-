/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */

package unoxtutti.domain; 
 
import unoxtutti.connection.P2PConnection; 
 
/** 
 * 
 * @author Riccardo Rossi 
 */ 
public class RemoteMatch { 
    
    private final Player owner; 
    private final P2PConnection p2p; 
    
    RemoteMatch(Player owner, P2PConnection p2p) {
        this.owner = owner; 
        this.p2p = p2p; 
    }
    
    static RemoteMatch createRemoteMatch(Player owner, P2PConnection p2p) { 
        return new RemoteMatch(owner, p2p); 
    }
    
} 
