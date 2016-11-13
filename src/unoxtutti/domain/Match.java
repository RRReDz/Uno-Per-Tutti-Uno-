/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

/**
 * Rappresentazione astratta di una Partita.
 * @author Davide
 */
public abstract class Match {
    /**
     * Tipi di messaggio
     */
    public static final String MATCH_CREATION_REQUEST_MSG = "matchCreationRequest";
    public static final String MATCH_CREATION_REPLY_MSG = "matchCreationReply";
    public static final String MATCH_UPDATE_MSG = "matchUpdate";
    public static final String MATCH_EXIT_MSG = "matchExit";
    public static final String MATCH_DESTROY_MSG = "matchDestroy";
    
    /**
     * Nome della partita
     */
    protected final String matchName;
    
    /**
     * Impostazioni della partita
     */
    protected final Object options;
    
    /**
     * Costruttore
     * @param name Nome
     * @param options Opzioni
     */
    protected Match(String name, Object options) {
        this.matchName = name;
        this.options = options;
    }
    
    /**
     * Ritorna il nome della partita
     * @return Nome della partita
     */
    public String getMatchName() {
        return matchName;
    }
}
