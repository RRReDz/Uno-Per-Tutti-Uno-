/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import unoxtutti.connection.InvalidRequestException;
import unoxtutti.connection.PlayerWonException;
import unoxtutti.connection.StatusChangedException;
import unoxtutti.utils.MapUtils;

/**
 * Rappresenta lo stato di una partita lato server.
 * @author Davide
 */
public class ServerMatchStatus extends MatchStatus {
    /**
     * Mazzo di pesca.
     */
    private final Deck mazzoPesca;
    
    /**
     * Mani (carte possedute) di ogni giocatore.
     */
    private final HashMap<Player, Collection<Card>> mani;
    
    /**
     * Indica l'ultimo giocatore che ha scartato una carta.
     */
    private Player previousPlayer;
    
    /**
     * Indica se il giocatore precedente ha dichiarato UNO!.
     */
    private boolean previousPlayerDeclaredUno;
    
    /**
     * Inizializzazione dello stato di una partita
     * @param players Giocatori partecipanti
     */
    ServerMatchStatus(List<Player> players) {
        super();
        super.trackEvent("La partita è stata avviata.");
        
        /* Inizializzazione ordine turni */
        /* Mazzo temporaneo per stabilire l'ordine dei turni*/
        super.trackEvent("Inizializzazione ordine dei turni...");
        Deck rndDeck = new Deck();
        Map<Player, Card> randomCards = new HashMap<>();
        players.forEach((p) -> {
            Card c = rndDeck.pescaCarta();
            randomCards.put(p, c);
            String eventMessage = p.getName() + " ha pescato " + c + ", valore: " + c.getCardValue();
            super.trackEvent(eventMessage);
        });
        
        /* Si aggiorna l'ordine della lista dei turni */
        turns = new ArrayList<>();
        Map<Player, Card> orderedTurns = MapUtils.sortByValueReverseOrder(randomCards);
        orderedTurns.forEach((player, card) -> {
            turns.add(player);
        });
        
        /* Inizializzazione mazzi */
        mazzoPesca = new Deck();
        
        /* Inizializzazione giocatore */
        currentPlayer = turns.get(0);
        
        /* Inizializzazioni mani */
        super.trackEvent("Distribuzione delle carte...");
        mani = new HashMap<>();
        players.forEach((p) -> {
            mani.put(p, new ArrayList<>());
        });
        
        /* Si pescano 7 carte per ogni giocatore */
        for(int i = 0; i < 7; i++) {
            players.forEach((p) -> {
                mani.get(p).add(mazzoPesca.pescaCarta());
            });
        }
        super.trackEvent("Le carte sono state distribuite.");
        
        /* Inizializzazione dichiarazioni UNO */
        previousPlayer = null;
        previousPlayerDeclaredUno = false;
        
        /* Carta iniziale sul tavolo, giocata dal server */
        cartaMazzoScarti = mazzoPesca.pescaCarta();
        super.trackEvent("È il turno di " + currentPlayer + ".");
        super.trackEvent("Carta sul tavolo: " + cartaMazzoScarti);
        
        /* Eventuale gestione della prima carta */
        if(cartaMazzoScarti.isJollyPescaQuattro()) {
            super.trackEvent(currentPlayer + " pesca 4 carte.");
            Collection<Card> mano = mani.get(currentPlayer);
            for(int i = 0; i < 4; i++) {
                mano.add(mazzoPesca.pescaCarta());
            }
            nextPlayer();
        } else if(cartaMazzoScarti.isPescaDue()) {
            super.trackEvent(currentPlayer + " pesca 2 carte.");
            Collection<Card> mano = mani.get(currentPlayer);
            for(int i = 0; i < 2; i++) {
                mano.add(mazzoPesca.pescaCarta());
            }
            nextPlayer();
        } else if(cartaMazzoScarti.isActionCard()) {
            handleCard();
        }
    }
    
    
    /**
     * Recupera le carte nella mano di un giocatore.
     * @param player Giocatore
     * @return Carte possedute dal giocatore
     */
    public Collection<Card> getCardsOfPlayer(Player player) {
        if(!turns.contains(player)) {
            throw new IllegalArgumentException("Il giocatore non è presente nella stanza.");
        }
        
        /* Carte possedute dal giocatore */
        return Collections.unmodifiableCollection(mani.get(player));
    }

    /**
     * Gestisce una richiesta di scarto carta.
     * @param player Giocatore richiedente
     * @param card Carta che il giocatore desidera scartare
     */
    synchronized void handlePlayCardRequest(Player player, Card card) 
            throws InvalidRequestException, StatusChangedException, PlayerWonException {
        /**
         * Indica se l'effetto della carta precedentemente
         * scartata è stato annullato. L'effetto di una carta azione può essere
         * annullato giocando una carta azione identica.
         */
        boolean previosActionCardEffectCanceled = false;
        
        Collection<Card> mano = mani.get(player);
        
        /* Innazitutto il giocatore deve possedere la carta */
        if(!mano.contains(card)) {
            throw new InvalidRequestException("Non possiedi la carta " + card.toString());
        }
        
        /* Se non è il turno del giocatore, si tratta di un interruzione di turno. */
        if(currentPlayer.equals(player)) {
            /* È il turno del giocatore */
            if(cardsToPick == 1) {
                /* Il giocatore non è afflitto da penalità */
                if(card.isJolly() && card.getColore() == Card.COLORE_NESSUNO) {
                    throw new InvalidRequestException("Non è stato impostato il colore del jolly.");
                }
                if(card.getDettaglio() != cartaMazzoScarti.getDettaglio() &&
                        card.getColore() != cartaMazzoScarti.getColore() &&
                        !card.isJolly()) {
                    throw new InvalidRequestException("Non puoi scartare un " 
                            + card + ",\n\nSul tavolo è presente un " + cartaMazzoScarti + ".");
                }
            } else {
                /**
                 * Il giocatore è afflitto da penalità:
                 * può annullare l'effetto della carta azione e non pescare
                 * le carte giocando una carte identica a quella sul tavolo.
                 */
                if(card.isJollyPescaQuattro()) {
                    throw new InvalidRequestException("Non puoi anunllare l'effetto "
                            + "del " + card);
                }
                if(!card.equals(cartaMazzoScarti)) {
                    throw new InvalidRequestException("Non puoi annullare l'effetto "
                            + "del " + cartaMazzoScarti + " giocando un " + card + ".");
                }
                
                previosActionCardEffectCanceled = true;
            }
        } else {
            /* Interruzione di turno */
            if(cartaMazzoScarti.isJolly()) {
                throw new InvalidRequestException("Non puoi interrompere il turno su una carta jolly.");
            }
            if(!card.equals(cartaMazzoScarti)) {
                throw new InvalidRequestException("Non è possibile interrompere "
                        + "il turno con un " + card.toString());
            }
            
            /**
             * Se l'interruzione avviene su una carta di tipo azione
             */
            if(card.isActionCard()) {
                previosActionCardEffectCanceled = true;
            }
            
            /* Il giocatore si impadrona del turno */
            currentPlayer = player;
        }
        
        /* La carta viene scartata */
        trackEvent(currentPlayer + " scarta un " + card);
        mano.remove(card);
        cartaMazzoScarti = card;
        
        /* Reset Dichiarazione UNO e aggiornamento ultimo giocatore */
        previousPlayer = currentPlayer;
        previousPlayerDeclaredUno = false;
        
        /* Se l'effetto di una carta azione non è stato annullato, viene gestita */
        if(!previosActionCardEffectCanceled) {
            handleCard();
        } else {
            trackEvent("Gli effetti delle due carte azione si annullano a vicenda.");
            
            /* Si ripristina il senso di marcia se necessario */
            if(card.isChangeDirection()) {
                changeDirection();
                trackEvent("Il senso di marcia è stato ripristinato.");
            }
            
            cardsToPick = 1;
            nextPlayer();
        }
        
        /**
         * Si verifica se il giocatore ha vinto la partita.
         */
        if(mano.isEmpty()) {
            throw new PlayerWonException();
        }
        
        throw new StatusChangedException();
    }

    /**
     * Gestisce una richiesta di tipo "Pesca carta/e".
     * @param player Giocatore richiedente
     */
    synchronized void handlePickCardRequest(Player player) throws InvalidRequestException, StatusChangedException {
        /* Il giocatore può pescare carte soltanto durante il proprio turno. */
        if(!currentPlayer.equals(player)) {
            throw new InvalidRequestException("Non è il tuo turno.");
        }
        
        /* Il giocatore pesca una o più carte */
        Collection<Card> mano = mani.get(player);
        for(int i = 0; i < cardsToPick; i++) {
            mano.add(mazzoPesca.pescaCarta());
        }
        
        /* Messaggio di notifica */
        if(cardsToPick == 1) {
            trackEvent(player.getName() + " ha pescato una carta.");
        } else {
            trackEvent(player.getName() + " ha pescato " + cardsToPick + " carte.");
        }
        
        /* Ripristino contatore */
        cardsToPick = 1;
        
        /* Giocatore successivo */
        nextPlayer();
        
        throw new StatusChangedException();
    }

    /**
     * Gestisce una richiesta di tipo "Dubita bluff".
     * @param player Giocatore richiedente
     */
    synchronized void handleCheckBluffRequest(Player player) throws InvalidRequestException, StatusChangedException {
        /* Solo il giocatore di turno può controllare un bluff */
        if(!player.equals(currentPlayer)) {
            throw new InvalidRequestException("Non è il tuo turno.");
        }
        
        /* Controllo che sul tavolo ci sia un Jolly Pesca Quattro */
        if(!cartaMazzoScarti.isJollyPescaQuattro()) {
            throw new InvalidRequestException("La carta sul tavolo deve essere un Jolly Pesca Quattro."
                    + "\n\nSul tavolo è presente un " + cartaMazzoScarti + ".");
        }
        
        /* Controllo che la carta sia stata appena scartata */
        if(cardsToPick == 1) {
            throw new InvalidRequestException("L'effetto del " + cartaMazzoScarti + " è già stato applicato.");
        }
        
        /**
         * Si recupera il giocatore che ha giocato il Jolly Pesca Quattro,
         * e al quale bisogna quindi controllare le carte perchè potrebbe
         * aver bluffato.
         */
        Player bluffer = previousPlayer;
        Collection<Card> mano = mani.get(bluffer);
        boolean hasCardOfSameColor = false;
        for (Iterator<Card> it = mano.iterator(); it.hasNext() && !hasCardOfSameColor;) {
            Card c = it.next();
            if(c.hasSameColorOf(cartaMazzoScarti)) {
                hasCardOfSameColor = true;
            }
        }
        
        trackEvent(player + " ha chiesto di controllare se " + bluffer + " ha bluffato...");
        if(hasCardOfSameColor) {
            /**
             * Il giocatore precedente ha bluffato: deve pescare 4 carte.
             * Il giocatore corrente non perde il turno.
             */
            Collection<Card> manoPenalita = mani.get(bluffer);
            for(int i = 0; i < 4; i++) {
                manoPenalita.add(mazzoPesca.pescaCarta());
            }
            trackEvent(bluffer + " è stato scoperto a bluffare: come penalità pesca 4 carte.");
        } else {
            /**
             * Il giocatore precedente non ha bluffato: quello corrente
             * deve pescare 6 carte e perde il turno.
             */
            Collection<Card> manoPenalita = mani.get(player);
            for(int i = 0; i < 6; i++) {
                manoPenalita.add(mazzoPesca.pescaCarta());
            }
            trackEvent(bluffer + " non ha bluffato: " + player + " pesca 6 carte.");
            nextPlayer();
        }
        
        throw new StatusChangedException();
    }

    /**
     * Gestisce una richiesta di tipo "Dichiara UNO!".
     * @param player Giocatore richiedente
     */
    synchronized void handleDeclareUNORequest(Player player) throws InvalidRequestException, StatusChangedException {
        /* Il giocatore deve aver scartato una carta il turno precedente */
        if(player.equals(previousPlayer)) {
            throw new InvalidRequestException("Puoi dichiarare UNO! solo dopo aver scartato una carta.");
        }
        
        /* Il giocatore deve avere soltanto una carta */
        if(mani.get(player).size() != 1) {
            throw new InvalidRequestException("Devi avere una carta per poter dichiarare UNO!");
        }
        
        /* Dichiarazione UNO */
        trackEvent(player + " ha dichiarato UNO!");
        previousPlayerDeclaredUno = true;
        
        throw new StatusChangedException();
    }
    
    
    /**
     * Gestisce una richiesta di tipo "Verifica dichiarazione UNO!".
     * @param player Giocatore richiedente
     */
    synchronized void handleCheckUNODeclarationRequest(Player player) throws InvalidRequestException, StatusChangedException {
        Collection<Card> mano = mani.get(previousPlayer);
        if(mano.size() == 1) {
            if(previousPlayerDeclaredUno) {
                /* Il giocatore precedente ha dichiarato UNO! correttamente */
                throw new InvalidRequestException(previousPlayer + " ha dichiarato UNO!");
            }
        } else {
            /* Il giocatore precedente ha più di una carta */
            throw new InvalidRequestException(previousPlayer + " ha più di una carta.");
        }
        
        /* Il giocatore precedente ha una carta e non ha dichiarato UNO! */
        trackEvent(player + " ha fatto notare che " + previousPlayer + " non ha "
                + "dichiarato UNO: per punizione pesca 2 carte.");
        for(int i = 0; i < 2; i++) {
            mano.add(mazzoPesca.pescaCarta());
        }
        
        throw new StatusChangedException();
    }
    
    
    /**
     * Inverte la direzionedei turni
     */
    private void changeDirection() {
        switch(turnsDirection) {
            case DIRECTION_FORWARD:
                turnsDirection = MatchStatus.DIRECTION_BACKWARD;
                break;
            case DIRECTION_BACKWARD:
                turnsDirection = MatchStatus.DIRECTION_FORWARD;
                break;
        }
    }
    
    
    /**
     * Assegna il turno al giocatore successivo.
     */
    private void nextPlayer() {
        int currentIndex = turns.indexOf(currentPlayer);
        if(turnsDirection == MatchStatus.DIRECTION_FORWARD) {
            currentIndex += 1;
            if(currentIndex == turns.size()) {
                currentIndex = 0;
            }
        } else {
            currentIndex -= 1;
            if(currentIndex < 0) {
                currentIndex = turns.size() - 1;
            }
        }
        
        currentPlayer = turns.get(currentIndex);
        trackEvent("È il turno di " + currentPlayer.getName() + ".");
    }

    /**
     * Gestisce la carta attualmente presente sul tavolo (in cima al
     * mazzo degli scarti).
     */
    private void handleCard() {
        /**
         * Se non si tratta di un "Cambio giro", possiamo benissimo
         * far passare il turno.
         */
        if(!cartaMazzoScarti.isChangeDirection()) {
            nextPlayer();
        }
        
        /* Gestione carte speciali */
        if(cartaMazzoScarti.isSpecialCard()) {
            if(cartaMazzoScarti.isChangeDirection()) {
                /**
                 * Il giocatore ha giocato un Cambia giro.
                 */
                trackEvent("Il senso di marcio è cambiato!");
                changeDirection();
                nextPlayer();
            } else if(cartaMazzoScarti.isJollyPescaQuattro()) {
                /**
                 * Il giocatore ha giocato un Jolly Pesca Quattro.
                 */
                cardsToPick = 4;
            } else if(cartaMazzoScarti.isPickTwo()) {
                /**
                 * Il giocatore ha giocato un Pesca due.
                 */
                if(cardsToPick == 1) {
                    cardsToPick = 2;
                } else {
                    cardsToPick += 2;
                }
            } else if(cartaMazzoScarti.isSkipTurn()) {
                /**
                 * Se la carta è di tipo "Salta Turno", il giocatore
                 * successivo salterà il proprio turno.
                 */
                super.trackEvent(currentPlayer.getName() + " salta il proprio turno.");
                nextPlayer();
            }
        }
    }
    
}
