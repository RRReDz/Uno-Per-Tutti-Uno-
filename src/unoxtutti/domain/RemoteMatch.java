/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazioni Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import unoxtutti.GiocarePartitaController;
import unoxtutti.UnoXTutti;
import unoxtutti.connection.CommunicationException;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PConnection;
import unoxtutti.connection.P2PMessage;
import unoxtutti.connection.PartnerShutDownException;
import unoxtutti.dialogue.MatchClosingDialogueHandler;
import unoxtutti.dialogue.MatchClosingDialogueState;
import unoxtutti.dialogue.MatchCreationDialogueHandler;
import unoxtutti.dialogue.MatchCreationDialogueState;
import unoxtutti.dialogue.MatchStartingDialogueHandler;
import unoxtutti.dialogue.MatchStartingDialogueState;
import unoxtutti.domain.dialogue.DialogueHandler;
import unoxtutti.domain.dialogue.DialogueObserver;
import unoxtutti.gui.GameplayPanel;
import unoxtutti.utils.DebugHelper;
import unoxtutti.utils.GUIUtils;

/**
 * Rappresenta una partita dal punto di vista dei client.
 * @author Davide
 */
public class RemoteMatch extends Match implements MessageReceiver, DialogueObserver {
    /**
     * Connessione con il proprietario della stanza in cui ci si trova.
     */
    private final P2PConnection conn;
    
    /**
     * Giocatore proprietario della partita
     */
    private final Player owner;
    
    /**
     * DialogueHandler per la creazione di partite.
     */
    private MatchCreationDialogueHandler creationHandler;
    
    /**
     * DialogueHandler per l'avvio della partita
     */
    private MatchStartingDialogueHandler startingHandler;
    
    /**
     * DialogueHandler per la chiusura della partita
     */
    private MatchClosingDialogueHandler closingHandler;
    
    /**
     * Lista di giocatori all'interno della partita.
     */
    private final DefaultListModel<Player> playersList;
    
    /**
     * Indica se la partita è stata avviata o meno.
     */
    private boolean isStarted = false;
    
    /**
     * Costruttore che memorizza le informazioni più importanti.
     * Questo costrutto viene utilizzato durante la creazione di una partita.
     * @param connectionToRoomHost Connessione con il proprietario della stanza.
     * @param matchName Nome della partita desiderato.
     * @param options Opzioni della partita.
     */
    private RemoteMatch(P2PConnection connectionToRoomHost, String matchName, Object options) {
        super(matchName, options);
        conn = connectionToRoomHost;
        owner = UnoXTutti.theUxtController.getPlayer();
        playersList = new DefaultListModel<>();
    }
    
    /**
     * Costruttore richiamato quando si accede ad una partita già esistente.
     * @param connectionToRoomHost Connessione con il gestore della partita.
     * @param matchOwner Proprietario della partita
     * @param matchName Nome della partita
     * @param options Opzioni
     */
    public RemoteMatch(P2PConnection connectionToRoomHost, Player matchOwner, String matchName, Object options) {
        super(matchName, options);
        conn = connectionToRoomHost;
        owner = matchOwner;
        playersList = new DefaultListModel<>();
    }
    
    
    /**
     * Tenta di creare una partita all'interno della stanza indicata.
     * @param matchName Nome della partita
     * @param options Opzioni di creazione della partita
     * @return Istanza di <code>RemoteMatch</code>, <code>null</code> in caso di
     * fallimento.
     */
    public static RemoteMatch createRemoteMatch(String matchName, Object options) {
        P2PConnection conn = GiocarePartitaController.getInstance().getCurrentRoom().getConnection();
        RemoteMatch m = new RemoteMatch(
                conn,
                matchName,
                options
        );
        
        boolean success = m.create();
        if(success) {
            return m;
        }
        return null;
    }
    
    /**
     * Viene richiamato ogni qualvolta la P2PConnection notifica
     * questa istanza di un messaggio di tipo "MATCH_UPDATE_MSG"
     * @param msg messaggio ricevuto 
     */
    @Override
    public void updateMessageReceived(P2PMessage msg) {
        switch (msg.getName()) {
            case Match.MATCH_UPDATE_MSG: //Handler ricezione aggiornamento partita
                DebugHelper.log("Ricevuto aggiornamento dal server: PARTITA AGGIORNATA");
                handlePlayersUpdateMessage(msg);
                break;
            case Match.MATCH_ACCESS_REQUEST_MSG: // Richiesta di accesso inoltrata dal RoomServer
                DebugHelper.log("Ricevuto inoltro richiesta di accesso dal server.");
                handleAccessRequestMessage(msg);
                break;
            case Match.MATCH_STARTED_MSG: //Questo sarà l'handler dell'avvio del match
                DebugHelper.log("Ricevuto aggiornamento dal server: PARTITA INIZIATA.");
                handleMatchStartedMessage(msg);
                break;
            case Match.MATCH_CLOSED_MSG: //Questo sarà l'handler della chiusura del match
                DebugHelper.log("Ricevuto aggiornamento dal server: PARTITA CHIUSA.");
                handleMatchClosedMessage(msg);
                break;
            case MatchStatus.STATUS_UPDATE_MSG:
                DebugHelper.log("Ricevuto aggiornamento dal server: STATO AGGIORNATO");
                handleStatusUpdateMessage(msg);
                break;
            case MatchStatus.STATUS_ERROR_MESSAGE:
                try {
                    String errorMessage = (String) msg.getParameter(0);
                    DebugHelper.log("Errore ricevuto dal server: " + errorMessage);
                    GUIUtils.showErrorMessage(UnoXTutti.mainWindow, errorMessage, "Notifica dal server");
                } catch(ClassCastException ex) {
                    /* Messaggio di notifica errato */
                    Logger.getLogger(RemoteMatch.class.getName()).log(Level.SEVERE, null, ex);
                }
            default:
        }
    }
    
    /**
     * Gestisce il cambio di stato di un dialogo.
     * @param source DialogueHandler generatore dell'evento.
     */
    @Override
    public void updateDialogueStateChanged(DialogueHandler source) {
        /* Handler della creazione della partita */
        if(source.equals(creationHandler)) {
            handleMatchCreationRequest(creationHandler);
        }
        /* Handler dell'avvio della partita */
        else if(source.equals(startingHandler)) {
            handleMatchStartingRequest(startingHandler);
        }
        /* Handler dell'avvio della partita */
        else if(source.equals(closingHandler)) {
            handleMatchClosingRequest(closingHandler);
        }
    }
    
    
    /**
     * Gestisce il cambiamento di stato dell'handler per la creazione
     * di partite.
     * @param source Handler della richiesta
     */
    private void handleMatchCreationRequest(MatchCreationDialogueHandler source) {
        MatchCreationDialogueState state = source.getState();
        switch (state) {
            case ADMITTED:
                DebugHelper.log("Risposta da RoomServer: OK! La partita è stata creata.");
                creationHandler.concludeDialogue();
                GiocarePartitaController.getInstance().matchCreationCompleted(this);
                break;
            case REJECTED:
                DebugHelper.log("Risposta da RoomServer: ERR! Impossibile creare la partita.");
                creationHandler.concludeDialogue();
                GiocarePartitaController.getInstance().matchCreationFailed(this);
                break;
            default:
        }
    }
    
    /**
     * Gestisce il cambiamento di stato dell'handler per l'avvio della partita.
     * @param source Handler della richiesta 
     */
    private void handleMatchStartingRequest(MatchStartingDialogueHandler source) {
        MatchStartingDialogueState state = source.getState();
        switch(state) {
            case STARTED:
                /**
                 * Solo in questo caso abbiamo una vera conferma se
                 * il ServerMatch è partito o meno.
                 */
                isStarted = true;
                DebugHelper.log("Risposta da MatchServer: OK! La partita è stata avviata.");
                break;
            case NOT_STARTED:
                /**
                 * Solo in questo caso abbiamo una vera conferma se
                 * il ServerMatch è partito o meno.
                 */
                isStarted = false;
                DebugHelper.log("Risposta da MatchServer: ERR! Impossibile avviare la partita.");
                break;
        }
        creationHandler.concludeDialogue();
        GiocarePartitaController.getInstance().wakeUpController();
    }
    
    /**
     * Gestisce il cambiamento di stato dell'handler per la chiusura della partita.
     * @param source Handler della richiesta 
     */
    private void handleMatchClosingRequest(MatchClosingDialogueHandler source) {
        MatchClosingDialogueState state = source.getState();
        
        /* Risposta ricevuta, fine dialogo */
        if(state == MatchClosingDialogueState.CLOSED || 
                state == MatchClosingDialogueState.NOT_CLOSED) {
            closingHandler.concludeDialogue();
        }
        
        /* Gestione risposta */
        switch(state) {
            case CLOSED:
                DebugHelper.log("Risposta da MatchServer: OK! La partita è stata chiusa.");
                GiocarePartitaController.getInstance().matchClosed();
                break;
            case NOT_CLOSED:
                DebugHelper.log("Risposta da MatchServer: ERR! Impossibile chiudere la partita.");
                GiocarePartitaController.getInstance().wakeUpController();
                break;
        }
    }
    
    /**
     * Avvia il dialogo con il server per creare una partita.
     * @return <code>true</code> se il dialogo è stato avviato con successo,
     *          <code>false</code> altrimenti.
     */
    private boolean create() {
        creationHandler = new MatchCreationDialogueHandler(conn);
        conn.addMessageReceivedObserver(this, Match.MATCH_UPDATE_MSG);
        creationHandler.addStateChangeObserver(this);
        return creationHandler.startDialogue(owner, matchName, options);
    }
    
    /**
     * Recupera la lista dei giocatori all'interno della partita
     * @return playersList lista di giocatori
     */
    public ListModel<Player> getPlayersAsList() {
        return playersList;
    }
    
    
    /**
     * Indica se il giocatore corrente è il proprietario della partita
     * @return <code>true</code> se l'utente è il proprietario della partita,
     *          <code>false</code> altrimenti
     */
    public boolean amITheOwner() {
        return owner != null && owner == UnoXTutti.theUxtController.getPlayer();
    }
    

    /** 
     * Avvia il dialogo con il server per avviare una partita.
     * @return <code>true</code> se il dialogo è stato avviato con successo,
     *          <code>false</code> altrimenti.
     **/
    public boolean startMatch() {
        startingHandler = new MatchStartingDialogueHandler(conn);
        startingHandler.addStateChangeObserver(this);
        /**
         * TODO: Ricevere OK dal server
         */
        return startingHandler.startDialogue(matchName);
    }
    
    /**
     * Avvia il dialogo con il server per chiudere la partita.
     * @return <code>true</code> se il dialogo è stato avviato con successo,
     *         <code>false</code> altrimenti.
     */
    public boolean closeMatch() {
        closingHandler = new MatchClosingDialogueHandler(conn);
        closingHandler.addStateChangeObserver(this);
        return closingHandler.startDialogue(matchName);
    }

    /**
     * @return isStarted <code>true</code> se il match è stato avviato
     * <code>false</code> altrimenti
     */
    public boolean isStarted() {
        return isStarted;
    }
    
    /**
     * Gestione di un messaggio di aggiornamento
     * @param msg Messaggio di aggiornamento
     */
    private void handlePlayersUpdateMessage(P2PMessage msg) {
        try {
            /* Aggiornamento lista giocatori */
            List<Player> players = (List<Player>) msg.getParameter(0);
            playersList.removeAllElements();
            players.forEach((p) -> {
                playersList.addElement(p);
            });
        } catch (ClassCastException ex) {
            throw new CommunicationException("Wrong parameter type in message " + msg.getName());
        }
    }

    /**
     * Gestione di una richiesta di accesso inoltrata dal RoomServer.
     * @param msg Richiesta di accesso
     */
    private void handleAccessRequestMessage(P2PMessage msg) {
        if(!owner.equals(UnoXTutti.theUxtController.getPlayer())) {
            /**
             * Non sono il proprietario, non dovrei neanche
             * ricevere questa notifica
             */
            throw new IllegalStateException("Il giocatore non è il proprietario della partita.");
        }
        
        try {
            Player applicant = (Player) msg.getParameter(0);
            DebugHelper.log("Devo decidere se accettare il giocatore " + applicant.getName());
            int answer = GUIUtils.askYesOrNoQuestion(
                    UnoXTutti.mainWindow,
                    "Vuoi accettare il giocatore " + applicant.getName() + " nella partita?",
                    "Richiesta di accesso"
            );
            /**
             * answer = 0 -> giocatore accettato (click su "Sì", bottone più a sinistra)
             * answer = 1 -> giocatore rifiutato (click su "No", bottone più a destra)
             */
            Boolean playerAccepted = answer == 0;
            
            /* Si comunicano al server le intenzioni del giocatore */
            try {
                P2PMessage response = new P2PMessage(Match.MATCH_ACCESS_REQUEST_REPLY_MSG);
                Object[] pars = new Object[]{ applicant, playerAccepted };
                response.setParameters(pars);
                conn.sendMessage(response);
            } catch (PartnerShutDownException ex) {
                GUIUtils.showErrorMessage(
                        UnoXTutti.mainWindow,
                        "Non è possibile connettersi con il proprietario della stanza.\n"
                            + "L'applicazione verrà chiusa."
                );
                System.exit(1);
            }
        } catch (ClassCastException ex) {
            throw new CommunicationException("Wrong parameter type in message " + msg.getName());
        }
    }

    /**
     * Gestione del messaggio ricevuto dal server relativo all'inizio della partita.
     * @param msg 
     */
    private void handleMatchStartedMessage(P2PMessage msg) {
        GiocarePartitaController.getInstance().matchStarted();
        /* Avvio della partita... fine iterazione 4 */
    }

    private void handleMatchClosedMessage(P2PMessage msg) {
        /**
         * TODO eliminare questo match dalla propria room
         * Questo verrà fatto dall'utente partecipante 
         * che riceve il messaggio di chiusura.
         */
        GUIUtils.showInformationMessage(UnoXTutti.mainWindow, "La partita è stata chiusa!");
        GiocarePartitaController.getInstance().receivedMatchClosure();
    }
    
    
    /**
     * Ricevuto un messaggio di cambio di stato da parte del server.
     * @param msg Messaggio di aggiornamento.
     */
    private void handleStatusUpdateMessage(P2PMessage msg) {
        try {
            if(msg.getParametersCount() != 2) {
                throw new IllegalArgumentException("Numero errato di argomenti: " + msg.getParametersCount());
            }
            
            MatchStatus status = (MatchStatus) msg.getParameter(0);
            ArrayList<Card> mano = (ArrayList<Card>) msg.getParameter(1);
            
            /* Aggiornamento interfaccia grafica */
            GameplayPanel userInterface = GiocarePartitaController.getInstance().gameplayPanel;
            userInterface.updateTurns(status);
            userInterface.updateCards(mano, status);
            userInterface.updateEvents(status);
            
        } catch(ClassCastException e) {
            Logger.getLogger(RemoteMatch.class.getName()).log(Level.SEVERE, null, e);
            throw new CommunicationException("Wrong parameter type in message " + msg.getName());
        }
    }
    
    /**
     * Informa il server che il giocatore desidera scartare una carta.
     * @param card Carta che si desidera scartare.
     */
    public void playCard(Card card) {
        sendActionMessage(MatchStatus.STATUS_PLAY_CARD_MSG, new Object[] { card });
    }
    
    /**
     * Informa il server che il giocatore desidera pescare una o più carte.
     */
    public void pickCard() {
        sendActionMessage(MatchStatus.STATUS_PICK_CARD_MSG, null);
    } 
    
    /**
     * Informa il server che il giocatore desidera verificare un bluff.
     */
    public void checkBluff() {
        sendActionMessage(MatchStatus.STATUS_CHECK_BLUFF_MSG, null);
    }
    
    /**
     * Informa il server che il giocatore desidera dichiarare UNO!.
     */
    public void declareUNO() {
        sendActionMessage(MatchStatus.STATUS_DECLARE_UNO_MSG, null);
    }
    
    /**
     * Invia un messaggio al Room Server.
     * @param messageType Tipo di messaggio da inviare
     * @param parameters Eventuali parametri da allegare al messaggio
     */
    private void sendActionMessage(String messageType, Object[] parameters) {
        try {
            P2PMessage msg = new P2PMessage(messageType);
            
            if(parameters != null) {
                msg.setParameters(parameters);
            }
            
            conn.sendMessage(msg);
        } catch (PartnerShutDownException ex) {
            Logger.getLogger(RemoteMatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
