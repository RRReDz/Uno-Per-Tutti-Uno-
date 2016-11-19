/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

/**
 * Rappresenta una partita lato server.
 * 
 * @author Davide
 */
public class ServerMatch extends Match {
    /**
     * Proprietario della partita
     */
    protected final Player owner;
    
    /**
     * Indica se la partita è stata avviata
     */
    protected boolean started;
    
    /**
     * Inizializza una partita
     * @param owner
     * @param name
     * @param options 
     */
    public ServerMatch(Player owner, String name, Object options) {
        super(name, options);
        this.owner = owner;
        this.started = false;
    }
    
    /**
     * Ritorna il creatore della partita
     * @return Proprietario della partita
     */
    public Player getOwner() {
        return owner;
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
     * Controlla se un determinato giocatore potrebbe entrare nella partita.
     * @param player Giocatore che desidera effettuare l'accesso
     * @return <code>true</code> se tutto va bene, <code>false</code> altrimenti.
     */
    boolean checkIfPlayerCanJoin(Player player) {
        return true;
    }
    
    
    /**
     * Chiede al proprietario della partita se un determinato giocatore
     * può entrare.
     * @param player Giocatore che desidera effettuare l'accesso.
     */
    void askOwnerIfPlayerCanJoin(Player player) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
