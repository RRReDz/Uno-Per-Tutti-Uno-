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
    public static final String MATCH_STARTING_MSG = "matchStarting";
    public static final String MATCH_STARTING_REPLY_MSG = "matchStartingReply";
    public static final String MATCH_CLOSING_MSG = "matchClosing";
    public static final String MATCH_ACCESS_REQUEST_MSG = "matchAccessRequest";
    public static final String MATCH_ACCESS_REQUEST_REPLY_MSG = "matchAccessRequestReply";
    
    /**
     * Nome della partita
     */
    protected final String matchName;
    
    /**
     * Impostazioni della partita
     */
    protected Object options;
    
    /**
     * Costruttore utilizzato durante la creazione
     * @param name Nome
     * @param options Opzioni
     */
    protected Match(String name, Object options) {
        this.matchName = name;
        this.options = options;
    }
    
    
    /**
     * Costruttore utilizzato durante l'accesso
     * @param name Nome della partita 
     */
    protected Match(String name) {
        this.matchName = name;
    }
    
    /**
     * Ritorna il nome della partita
     * @return Nome della partita
     */
    public String getMatchName() {
        return matchName;
    }
}
