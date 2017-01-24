/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.configuration.GameConfig;
import unoxtutti.connection.CommunicationException;
import unoxtutti.connection.InvalidRequestException;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;
import unoxtutti.connection.PlayerWonException;
import unoxtutti.connection.StatusChangedException;
import unoxtutti.utils.DebugHelper;

/**
 * Rappresenta una partita lato server.
 * 
 * @author Davide
 */
public class ServerMatch extends Match implements MessageReceiver {
    /**
     * Stanza di appartenenza.
     */
    protected final ServerRoom room;
    
    /**
     * Proprietario della partita.
     */
    protected final Player owner;
    
    /**
     * Giocatori della partita.
     */
    protected final List<Player> players;
    
    /**
     * Lista dei giocatori che hanno richiesto l'accesso alla partita.
     */
    protected final List<Player> joinRequests;
    
    /**
     * Indica se la partita è stata avviata.
     */
    protected boolean started;
    
    /**
     * Indica se la partita è terminata.
     */
    protected boolean ended;
    
    /**
     * Tiene traccia dello stato della partita.
     */
    protected ServerMatchStatus status;
    
    /**
     * Tiene traccia del numero di timeout effettuati da ogni giocatore.
     */
    protected HashMap<Player, Integer> timeouts;
    
    /**
     * Timer per la terminazione forzata dei turni.
     */
    protected Timer timeoutChecker;
    
    /**
     * Conta il numero di turni.
     */
    protected int turnId;
    
    /**
     * Contatore degli aggiornamenti inviati.
     */
    protected int updateId;
    
    /**
     * Inizializza una partita
     * @param parentRoom Stanza di appartenenza
     * @param owner Proprietario della partita
     * @param name Nome
     * @param options Opzioni
     */
    public ServerMatch(ServerRoom parentRoom, Player owner, String name, Object options) {
        /* Informazioni generali */
        super(name, options);
        this.room = parentRoom;
        this.owner = owner;
        
        /* Stato della partita */
        this.started = false;
        this.ended = false;
        
        /* Giocatori presenti e richieste di ingresso */
        this.players = new ArrayList<>();
        this.joinRequests = new ArrayList<>();
        
        /* Timer per la terminazione forzata dei turni */
        this.timeouts = new HashMap<>();
        this.timeoutChecker = new Timer();
        
        /* Contatori di turno ed aggiornamento */
        this.turnId = 1;
        this.updateId = 1;
        
        /* Il proprietario è già presente nella partita */
        players.add(owner);
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
     * Ritorna i giocatori della partita
     * @return Lista dei giocatori presenti nella partita
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * Utilizzato per sapere se il giocatore
     * è il proprietario della partita o meno.
     * @param player
     * @return 
     */
    public boolean isThisPlayerTheOwner(Player player) {
        return owner.equals(player);
    }
    
    /**
     * Metodo per notificare a tutti i giocatori in stanza l'inzio della partita.
     */
    private void notifyMatchStart() {
        P2PMessage matchStartedMsg = new P2PMessage(Match.MATCH_STARTED_MSG);
        List<P2PConnection> lostConnections = new ArrayList<>(players.size());
       
        synchronized(room) {
            /* Invio la risposta ad ogni utente */
            players.stream().filter((p) -> !isThisPlayerTheOwner(p)).map((p) -> room.getConnectionWithPlayer(p))
                    .forEach((playerConnection) -> {
                        try {
                            playerConnection.sendMessage(matchStartedMsg);
                        } catch (PartnerShutDownException ex) {
                            Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
                            DebugHelper.log("ERR: Il giocatore '" + playerConnection.getPlayer() + "' non è disponibile.");
                            lostConnections.add(playerConnection);
                        }
                    });
            
            /* Chiusura connessioni morte */
            lostConnections.stream().map((c) -> {
                c.disconnect();
                return c;
            }).forEachOrdered((c) -> {
                room.removePlayer(c);
            });
            
            /* Rimozione giocatori disconnessi e, in tal caso, ri-aggiornamento */
            lostConnections.forEach((c) -> {
                players.remove(c.getPlayer());
            });
            
            /**
             * Se ho trovato dei giocatori non più disponibili,
             * mando un aggiornamento dei giocatori alla stanza ed alla partita
             */
            if(lostConnections.size() > 0) {
                sendMatchUpdate();
                room.sendRoomUpdate();
            }
            
        }
    }
    
    /**
     * Metodo per notificare tutti gli utenti della chiusura della partita.
     */
    void notifyMatchClosure() {
        P2PMessage matchClosedMsg = new P2PMessage(Match.MATCH_CLOSED_MSG);
        List<P2PConnection> lostConnections = new ArrayList<>(players.size());
        
        synchronized(room) {
            /* Invio la notifica ad ogni utente */
            players.stream().filter((p) -> !isThisPlayerTheOwner(p)).map((p) -> room.getConnectionWithPlayer(p))
                    .forEach((playerConnection) -> {
                        try {
                            playerConnection.sendMessage(matchClosedMsg);
                        } catch (PartnerShutDownException ex) {
                            Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
                            DebugHelper.log("ERR: Il giocatore '" + playerConnection.getPlayer() + "' non è disponibile.");
                            lostConnections.add(playerConnection);
                        }
                    });
            /*players.stream().map((p) -> room.getConnectionWithPlayer(p)).forEach((playerConnection) -> {
                try {
                    playerConnection.sendMessage(matchClosedMsg);
                } catch (PartnerShutDownException ex) {
                    Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
                    DebugHelper.log("ERR: Il giocatore '" + playerConnection.getPlayer() + "' non è disponibile.");
                    lostConnections.add(playerConnection);
                }
            });*/
            
            /* Chiusura connessioni morte */
            lostConnections.stream().map((c) -> {
                c.disconnect();
                return c;
            }).forEachOrdered((c) -> {
                room.removePlayer(c);
            });
            
            /* Rimozione giocatori disconnessi e, in tal caso, ri-aggiornamento */
            lostConnections.forEach((c) -> {
                players.remove(c.getPlayer());
            });
            
            room.sendRoomUpdate();
            
        }
    }
    
    /**
     * Controlla se un determinato giocatore potrebbe entrare nella partita.
     * @param player Giocatore che desidera effettuare l'accesso
     * @return <code>true</code> se tutto va bene, <code>false</code> altrimenti.
     */
    public boolean canPlayerJoin(Player player) {
        return !(players.contains(player) || joinRequests.contains(player) || player.equals(owner));
    }
    
    
    /**
     * Chiede al proprietario della partita se un determinato giocatore
     * può entrare.
     * @param player Giocatore che desidera effettuare l'accesso.
     * @return <code>true</code> se la richiesta è stata inoltrata con successo,
     *          <code>false</code> altrimenti.
     */
    public boolean askOwnerIfPlayerCanJoin(Player player) {
        /* Abbiamo già fatto questo controllo, ma lo rifacciamo. */
        if(!canPlayerJoin(player)) return false;
        
        /**
         * Dato che questo metodo è richiamato da RoomServer, e RoomServer non
         * ha tempo da perdere, si comunica al giocatore la richiesta e poi
         * non si rimane in attesa di una risposta (dato che non è istantanea).
         */
        
        /* Preparazione del messaggio */
        P2PMessage msg = new P2PMessage(Match.MATCH_ACCESS_REQUEST_MSG);
        Object[] pars = new Object[]{ player };
        msg.setParameters(pars);
        P2PConnection conn = room.getConnectionWithPlayer(owner);
        
        /* Invio del messaggio al proprietario */
        boolean success = true;
        try {
            conn.sendMessage(msg);
            joinRequests.add(player);
            if(joinRequests.size() == 1) {
                conn.addMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
            }
        } catch (PartnerShutDownException ex) {
            success = false;
            conn.disconnect();
            room.removePlayer(conn);
        }
        return success;
    }
    
    
    /**
     * Ricevuto un messaggio di risposta da parte dell'owner della partita
     * @param msg Messaggio
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        synchronized(this) {
            Player sender = msg.getSenderConnection().getPlayer();
            Player playerAtTheStartOfTheTurn = null;
            
            if(started) {
                playerAtTheStartOfTheTurn = status.currentPlayer;
            }
            
            try {
                if(!players.contains(sender)) {
                    throw new InvalidRequestException("Il giocatore non appartiene alla partita.");
                }

                /* Se la partita è terminata, si ignora il messaggio */
                if(ended) { return; }

                switch(msg.getName()) {
                    case Match.MATCH_ACCESS_REQUEST_REPLY_MSG:
                        /* Messaggio di risposta del proprietario ad una richiesta di accesso */
                        handleMatchAccessAnswer(msg);
                        break;
                    case MatchStatus.STATUS_PLAY_CARD_MSG:
                        /**
                         * Il giocatore desidera giocare una carta.
                         * 
                         * Si verifica che come parametro sia passata una carta.
                         */
                        boolean validRequest = true;
                        Card card = null;
                        try {
                            if(msg.getParametersCount() != 1) {
                                validRequest = false;
                            } else {
                                card = (Card) msg.getParameter(0);
                            }
                        } catch (ClassCastException e) {
                            validRequest = false;
                        }
                        if (!validRequest) {
                            throw new InvalidRequestException(
                                "La richiesta inviata è stata ignorata in quanto "
                                        + " contenente argomenti non validi o errati."
                            );
                        }

                        /* Si gestisce la richiesta */
                        status.handlePlayCardRequest(sender, card);
                        break;
                    case MatchStatus.STATUS_PICK_CARD_MSG:
                        /* Il giocatore desidera pescare una o più carte */
                        status.handlePickCardRequest(sender);
                        break;
                    case MatchStatus.STATUS_CHECK_BLUFF_MSG:
                        /* Il giocatore desidera controllare un bluff */
                        status.handleCheckBluffRequest(sender);
                        break;
                    case MatchStatus.STATUS_DECLARE_UNO_MSG:
                        /* Il giocatore desidera dichiarare UNO! */
                        status.handleDeclareUNORequest(sender);
                        break;
                    case MatchStatus.STATUS_CHECK_UNO_DECLARATION:
                        /* Il giocatore si lamenta di un giocatore che non ha dichiarato UNO */
                        status.handleCheckUNODeclarationRequest(sender);
                        break;
                }
            } catch(InvalidRequestException ex) {
                /* Richiesta non valida */
                try {
                    /* Si notifica il giocatore dell'errore */
                    P2PMessage errorNotificationMessage = new P2PMessage(MatchStatus.STATUS_ERROR_MESSAGE);
                    errorNotificationMessage.setParameters(new Object[] { ex.getMessage() });
                    msg.getSenderConnection().sendMessage(errorNotificationMessage);
                } catch (PartnerShutDownException psde) {
                    Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, psde);
                }
            } catch(StatusChangedException ex) {
                /* Incremento turno, se non tocca più allo stesso giocatore */
                String msgType = msg.getName();
                if(msgType.equals(MatchStatus.STATUS_PLAY_CARD_MSG) ||
                        msgType.equals(MatchStatus.STATUS_PICK_CARD_MSG)) {
                    /* Se un giocatore gioca una carta o pesca, di sicuro il turno è finito */
                    turnId++;
                } else if(msgType.equals(MatchStatus.STATUS_CHECK_BLUFF_MSG) &&
                        !status.currentPlayer.equals(playerAtTheStartOfTheTurn)) {
                    /**
                     * Un altro modo per far finire il turno è richiamare
                     * un bluff, senza però avere successo.
                     * 
                     * Se il giocatore è cambiato dall'inizio del turno,
                     * significa che chi ha voluto controllare il bluff
                     * ha pescato 6 carte e perso il turno.
                     */
                    turnId++;
                }
                
                /* Stato cambiato */
                sendStatusUpdate();
            } catch (PlayerWonException ex) {
                /* Partita terminata: sender ha vinto */
                ended = true;
                sendStatusUpdate();
                
                /* Notifica di terminazione partita */
                notifyMatchEnded(sender);
                
                /* Si elimina la partita dalla stanza */
                room.matchEnded(this);
            }
        }
    }
    
    
    /**
     * Il proprietario della partita ha comunicato il proprio giudizio su 
     * un giocatore.
     * @param msg Messaggio
     */
    private void handleMatchAccessAnswer(P2PMessage msg) {
        try {
            if(msg.getParametersCount() != 2) return;
            Player matchOwner = msg.getSenderConnection().getPlayer();
            Player applicant = (Player) msg.getParameter(0);
            boolean accepted = (boolean) msg.getParameter(1);
            
            /* Controllo validità messaggio */
            if(!this.owner.equals(matchOwner)) {
                throw new IllegalStateException("Il giocatore non è il proprietario della partita.");
            }
            
            synchronized(room) {
                if(!joinRequests.contains(applicant)) {
                    /**
                     * Il giocatore non è nella lista delle richieste: potrebbe essere
                     * stato rimosso da essa in quanto è entrato in un'altra partita.
                     */
                    return;
                }
                
                if(accepted && !room.playerIsInAMatch(applicant)) {
                    if(tellPlayerToJoin(applicant)) {
                        addPlayer(applicant);
                    }
                    /* Rimuovo tutte le sue richieste pendenti */
                    room.deleteAccessRequests(applicant);
                }
                
                /* Richiesta gestita, pulizia */
                joinRequests.remove(applicant);
                if(joinRequests.isEmpty()) {
                    /* Non ci sono più richieste da gestire, si rimuove il listener */
                    msg.getSenderConnection().removeMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
                }
            }
        } catch (ClassCastException ex) {
            throw new CommunicationException("Wrong parameter type in message " + msg.getName());
        }
    }
    
    
    /**
     * Aggiunge un giocatore alla partita.
     * @param player Giocatore da aggiungere
     */
    protected void addPlayer(Player player) {
        if(players.contains(player)) {
            throw new IllegalStateException("Il giocatore è già presente nella partita.");
        }
        players.add(player);
        sendMatchUpdate();
    }
    
    
    /**
     * Rimuove un giocatore dalla partita
     * @param player Giocatore da rimuovere
     */
    protected void removePlayer(Player player) {
        if(!players.contains(player)) {
            throw new IllegalStateException("Il giocatore non è presente nella parita.");
        }
        players.remove(player);
        sendMatchUpdate();
    }
    
    
    /**
     * Manda un messaggio di aggiornamento a tutti i giocatori
     */
    void sendMatchUpdate() {
        synchronized(room) {
            /* Si manda un messaggio di aggiornamento a tutti i giocatori */
            P2PMessage upd = new P2PMessage(Match.MATCH_UPDATE_MSG);
            Object[] parameters = new Object[]{ this.getPlayers() };
            upd.setParameters(parameters);
            List<P2PConnection> lostConnections = new ArrayList<>(players.size());
            
            /* Si aggiorna ogni giocatore */
            players.stream().map((p) -> room.getConnectionWithPlayer(p)).forEachOrdered((c) -> {
                try {
                    c.sendMessage(upd);
                } catch (PartnerShutDownException ex) {
                    lostConnections.add(c);
                }
            });
            
            /* Chiusura connessioni morte */
            lostConnections.stream().map((c) -> {
                c.disconnect();
                return c;
            }).forEachOrdered((c) -> {
                room.removePlayer(c);
            });
            
            /* Rimozione giocatori disconnessi e, in tal caso, ri-aggiornamento */
            lostConnections.forEach((c) -> {
                players.remove(c.getPlayer());
            });
            if(lostConnections.size() > 0) {
                sendMatchUpdate();
                room.sendRoomUpdate();
            }
        }
    }
    
    
    /**
     * Indica se un determinato giocatore è presente nella partita
     * @param player Giocatore da cercare
     * @return <code>true</code> se il giocatore si trova nella partita,
     *          <code>false</code> altrimenti.
     */
    protected boolean containsPlayer(Player player) {
        return players.contains(player);
    }

    /**
     * Rimuove un giocatore dalla lista delle richieste
     * @param player Giocatore da rimuovere
     */
    void removePlayerAccessRequest(Player player) {
        joinRequests.remove(player);
        if(joinRequests.isEmpty()) {
            /* Inutile ascoltare messaggi se non ci sono richieste valide */
            room.getConnectionWithPlayer(owner)
                    .removeMessageReceivedObserver(this, Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
        }
    }

    /**
     * Informa il giocatore richiedente che la sua richiesta di accesso
     * ad una partita è stata accettata.
     * 
     * Il metodo sposta dunque il giocatoe all'interno della partita.
     * @param player Giocatore
     * @return <code>true</code> se il messaggio viene inviato con successo,
     *          <code>false</code> altrimenti.
     */
    private boolean tellPlayerToJoin(Player player) {
        P2PConnection conn = room.getConnectionWithPlayer(player);
        P2PMessage message = new P2PMessage(Match.MATCH_ACCESS_SUCCESS_NOTIFICATION_MSG);
        message.setParameters(new Object[] { matchName, owner });
        // TODO: passare opzioni
        try {
            conn.sendMessage(message);
            return true;
        } catch (PartnerShutDownException ex) {
            conn.disconnect();
            room.removePlayer(conn);
            Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Richiamato quando la partita viene avviata.
     * 
     * Notifica tutti i giocatori dell'inizio della partita, tranne
     * il proprietario in quanto lui lo sa già: è lui a farla iniziare.
     */
    void start() {
        synchronized(this) {
            if(started) {
                throw new IllegalStateException("La partita è già stata avviata.");
            }

            started = true;
            notifyMatchStart();

            players.forEach((p) -> {
                /* Aggiornamento listeners */
                P2PConnection conn = room.getConnectionWithPlayer(p);
                conn.addMessageReceivedObserver(this, MatchStatus.STATUS_PLAY_CARD_MSG);
                conn.addMessageReceivedObserver(this, MatchStatus.STATUS_PICK_CARD_MSG);
                conn.addMessageReceivedObserver(this, MatchStatus.STATUS_CHECK_BLUFF_MSG);
                conn.addMessageReceivedObserver(this, MatchStatus.STATUS_DECLARE_UNO_MSG);
                conn.addMessageReceivedObserver(this, MatchStatus.STATUS_CHECK_UNO_DECLARATION);

                /* Inizializzazione contatore timeouts */
                timeouts.put(p, 0);
            });

            status = new ServerMatchStatus(players);
            sendStatusUpdate();
        }
    }
    
    /**
     * Invia a tutti i giocatori lo stato aggiornato della partita.
     * 
     * Ad ogni aggiornamento si resetta il timer di durata massima di un turno.
     */
    private void sendStatusUpdate() {
        synchronized(room) {
            /* Informazioni comuni a tutti i messaggi di aggiornamento */
            MatchStatus updatedStatus = status.creaCopia();
            
            /* Non usare operatori funzionali: è necessario aggiornare numberOfFailures */
            int numberOfFailures = 0;
            for(Player p : players) {
                try {
                     P2PConnection c = room.getConnectionWithPlayer(p);
                     
                    /* Per ogni giocatore, si costruisce un messaggio di aggiornamento apposito */
                    P2PMessage upd = new P2PMessage(MatchStatus.STATUS_UPDATE_MSG);

                    Object[] parameters = new Object[] {
                        updatedStatus,
                        new ArrayList<>(status.getCardsOfPlayer(c.getPlayer()))
                    };
                     
                    upd.setParameters(parameters);
                    c.sendMessage(upd);
                } catch (PartnerShutDownException ex) {
                    /**
                     * Si ignora il fatto che il giocatore abbia
                     * perso la connessione, per ora
                     */
                    numberOfFailures++;
                }
            }
            
            /* Incremento contatore */
            updateId++;
            
            /* Reset timer */
            if(!ended && numberOfFailures < players.size()) {
                /**
                 * Se numberOfFailures = players.size() significa che
                 * tutti i giocatori sono disconnessi: non ha senso
                 * creare un nuovo timer.
                 */
                timeoutChecker.schedule(new TimeoutChecker(this), GameConfig.TURN_MAXIMUM_LENGTH);
            }
        }
    }

    
    /**
     * Si comunica a tutti i giocatori che un giocatore ha vinto.
     * 
     * @param winner Vincitore
     */
    private void notifyMatchEnded(Player winner) {
        synchronized(room) {
            players.stream().map((p) -> room.getConnectionWithPlayer(p)).forEachOrdered((c) -> {
                try {
                    /* Il rapporto con il giocatore è terminato */
                    c.removeMessageReceivedObserver(this);
                    
                    /* Invio notifica */
                    P2PMessage upd = new P2PMessage(Match.MATCH_ENDED_MSG);
                    upd.setParameters(new Object[] { winner });
                    c.sendMessage(upd);
                } catch (PartnerShutDownException ex) {
                    /* Il giocatore se n'è andato, pazienza */
                    Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }
    
    
    /**
     * Richiamata da un task quando il giocatore corrente va in timeout.
     */
    void currentPlayerTimedOut() {
        try {
            Player timedoutPlayer = status.currentPlayer;
            
            /* Incremento contatore timeouts */
            int timeoutCounter = timeouts.get(timedoutPlayer) + 1;
            timeouts.put(timedoutPlayer, timeoutCounter);
            
            switch(timeoutCounter) {
                case 1:
                    status.trackEvent(timedoutPlayer + " è andato in timeout per la prima volta.");
                    break;
                case 2:
                    status.trackEvent(timedoutPlayer + " è andato in timeout per la seconda volta.");
                    break;
                case 3:
                    status.trackEvent(timedoutPlayer + " è andato in timeout per la terza volta.");
                    break;
                default:
                    status.trackEvent(timedoutPlayer + " è andato in timeout (contatore: " + timeoutCounter + ").");
            }
            
            /* Dopo 3 timeouts, il giocatore viene buttato fuori dalla partita. */
            if(timeoutCounter == GameConfig.MAXIMUM_TIMEOUTS) {
                // TODO: Buttare fuori il giocatore dalla partita
            }
            
            /* Il giocatore pesca una o più carte. */
            status.handlePickCardRequest(status.currentPlayer);
        } catch (InvalidRequestException ex) {
            /* È sempre possibile per un giocatore pescare una o più carte */
            Logger.getLogger(ServerMatch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (StatusChangedException ex) {
            /**
             * Corretto: il turno corrente è terminato perchè
             * il giocatore ha pescato una o più carte.
             */
            sendStatusUpdate();
        }
    }
}
