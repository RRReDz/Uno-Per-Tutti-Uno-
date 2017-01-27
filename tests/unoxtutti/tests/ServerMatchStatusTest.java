/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.tests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.*;
import unoxtutti.configuration.GameConfig;
import unoxtutti.connection.InvalidRequestException;
import unoxtutti.connection.PlayerWonException;
import unoxtutti.connection.StatusChangedException;
import unoxtutti.domain.Card;
import unoxtutti.domain.MatchStatus;
import unoxtutti.domain.Player;
import unoxtutti.domain.RegisteredPlayer;
import unoxtutti.domain.ServerMatchStatus;

/**
 * Tests per la classe ServerMatchStatus.
 * 
 * @author Davide
 */
public class ServerMatchStatusTest {
    /**
     * Lista di giocatori da inserire nella partita.
     */
    protected static List<Player> players;
    
    /**
     * Giocatori di esempio.
     */
    protected static Player davide;
    protected static Player riccardo;
    protected static Player leonardo;
    
    /**
     * Stato della partita.
     */
    protected ServerMatchStatus status;
    
    /**
     * Richiamata una volta prima dei tests.
     * 
     * Inizializza le variabili comuni.
     */
    @BeforeClass
    public static void setUpClass() {
        /* Creazione giocatori di esempio */
        davide = Player.createPlayer(new RegisteredPlayer(
                1,
                "Davide",
                "davide@unito.it",
                "password"
        ));
        riccardo = Player.createPlayer(new RegisteredPlayer(
                2,
                "Riccardo",
                "riccardo@unito.it",
                "password"
        ));
        leonardo = Player.createPlayer(new RegisteredPlayer(
                3,
                "Leonardo",
                "leonardo@unito.it",
                "password"
        ));
        
        /* Creazione lista giocatori */
        players = new ArrayList<>();
        players.add(davide);
        players.add(riccardo);
        players.add(leonardo);
    }
    
    /**
     * Richiamata prima di ogni test.
     */
    @Before
    public void setUp() {
        /* Si verifica che ci sia un solo costruttore */
        assertEquals(1, ServerMatchStatus.class.getDeclaredConstructors().length);
        
        /* Si crea uno stato standard (con i giocatori definiti in setUpClass) */
        status = createServerMatchStatus(players);
    }
    
    /**
     * Istanzia un oggetto di tipo ServerMatchStatus con i parametri indicati.
     * 
     * @param players Lista dei giocatori all'interno della partita.
     * 
     * @return Istanza di <code>ServerMatchStatus</code>.
     */
    protected ServerMatchStatus createServerMatchStatus(List<Player> players) {
        try {
            Constructor c = ServerMatchStatus.class.getDeclaredConstructors()[0];
            c.setAccessible(true);
            return (ServerMatchStatus) c.newInstance(players);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            fail("Impossibile istanziare la classe ServerMatchStatus.");
        }
        return null;
    }
    
    /**
     * Chiama un metodo d'istanza della classe ServerMatchStatus.
     * 
     * @param status Istanza su cui si vuole richiamare il metodo.
     * @param methodName Nome del metodo.
     * @param args Eventuali argomenti da passare al metodo.
     * 
     * @throws Exception Eventuale eccezione lanciata dal metodo richiamato.
     */
    protected void callMethod(ServerMatchStatus status, String methodName, Object... args) throws Exception {
        try {
            Method[] methods = ServerMatchStatus.class.getDeclaredMethods();
            for(Method m : methods) {
                if(m.getName().equals(methodName)) {
                    m.setAccessible(true);
                    m.invoke(status, args);
                    return;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            /**
             * Il metodo non è accessibile oppure gli argomenti passati sono errati.
             * 
             * Nota: il metodo è sempre accessibile perchè lo dichiariamo tale
             * con m.setAccessible(true).
             */
            fail("Impossibile richiamare il metodo \"" + methodName + "\": " + ex.getMessage() + ".");
        } catch (InvocationTargetException ex) {
            /**
             * Il metodo ha lanciato un'eccezione.
             */
            throw (Exception) ex.getTargetException();
        }
        fail("Metodo \"" + methodName + "\" non trovato.");
    }
    
    
    /**
     * Verifica la corretta inizializzazione dello stato della partita.
     */
    @Test
    public void testInitialization() {
        /* Verifica numero giocatori */
        assertEquals(players.size(), status.getTurns().size());
        
        /* Verifica assenza di penalità */
        assertEquals(status.getCardsToPick(), 1);
        
        /* Verifica che ogni giocatore abbia almeno le carte minime */
        players.stream().map((p) -> status.getCardsOfPlayer(p)).forEachOrdered((cards) -> {
            assertTrue(
                "Un giocatore non ha il numero minimo di carte (minimo: " + GameConfig.STARTING_CARDS +
                        ", possedute: " + cards.size() + ").",
                cards.size() >= GameConfig.STARTING_CARDS
            );
        });
        
        /* Si verifica che ci sia una carta sul tavolo */
        assertNotNull(status.getCartaMazzoScarti());
    }
    
    
    /**
     * Verifica il corretto cambio di senso di marcia.
     * @throws java.lang.Exception Eventuale eccezione lanciata da "callMethod".
     */
    @Test
    public void testChangeDirection() throws Exception {
        int original = status.getDirection();
        int expected = -1;
        
        switch(original) {
            case MatchStatus.DIRECTION_FORWARD:
                expected = MatchStatus.DIRECTION_BACKWARD;
                break;
            case MatchStatus.DIRECTION_BACKWARD:
                expected = MatchStatus.DIRECTION_FORWARD;
                break;
            default:
                fail("Il senso di marcia non è valido (" + original + ").");
        }
        
        /* Cambio di senso di marcia e verifica */
        for(int i = 0; i < 1000; i++) {
            callMethod(status, "changeDirection");
            assertEquals(expected, status.getDirection());
            callMethod(status, "changeDirection");
            assertEquals(original, status.getDirection());
        }
    }
    
    
    /**
     * Verifica che solo il giocatore di turno sia in grado di pescare carte.
     * 
     * @throws PlayerWonException quando la partita termina.
     */
    @Test(expected = PlayerWonException.class)
    public void testPickCard() throws PlayerWonException {
        /**
         * Fino al termine della partita, verifica che venga
         * pescato il numero corretto di carte ad ogni turno.
         */
        while(true) {
            /* Verifica che solo il giocatore di turno possa pescare le carte */
            players.stream().filter((p) -> (p != status.getCurrentPlayer())).forEachOrdered((p) -> {
                try {
                    callMethod(status, "handlePickCardRequest", p);
                } catch(InvalidRequestException ex) {
                    /* Come previsto, il giocatore non può pescare */
                } catch(Exception ex) {
                    fail("Eccezione inaspettata: " + ex.getClass().getSimpleName());
                }
            });
            
            playCardElsePick(status);
        }
    }
    
    
    /**
     * Verifica l'impossibile di controllare bluffs al primo turno.
     */
    @Test
    public void testCheckUNODeclaration() {
        /* Non è mai possibile verificare un bluff all'inizio della partita. */
        players.forEach((p) -> {
            try {
                callMethod(status, "handleCheckUNODeclarationRequest", p);
                fail("InvalidRequestException non lanciata.");
            } catch (Exception ex) {
                assertThat(ex.getMessage(), containsString("stato buttato fuori"));
            }
        });
    }
    
    
    /**
     * Verifica che le partite terminino quando un giocatore scarta la
     * sua ultima carta.
     * 
     * I giocatori eseguo azioni base all'infinito fino a quando la
     * partita non termina.
     * 
     * Ad ogni turno, il giocatore di turno prova a scartare una delle
     * sue carte, se non ci riesce, pesca una o più carte.
     * 
     * Durante il corso della partita vengono effettuati i controlli
     * all'interno dei metodi playCard e pickCard. Vengono dunque
     * verificate molte cose: per esempio che ad ogni turno venga pescato
     * il numero corretto di carte.
     * 
     * @throws PlayerWonException quando la partita termina.
     */
    @Test(expected = PlayerWonException.class)
    public void testEnd() throws PlayerWonException {
        while(true) {
            playCardElsePick(status);
        }
    }
    
    
    /**
     * Prova a far scartare una carta al giocatore corrente.
     * 
     * Se la carta viene scartata con successo, si verifica che nel mazzo
     * ci sia una carta in meno.
     * 
     * Se il giocatore vince la partita, si verifica che non abbia più carte.
     * 
     * @param status Stato della partita.
     * 
     * @return <code>true</code> se l'operazione è andata a buon fine,
     *          <code>false</code> altrimenti.
     * 
     * @throws PlayerWonException Quando il giocatore vince la partita.
     */
    protected boolean playCard(ServerMatchStatus status) throws PlayerWonException {
        Player currentPlayer = status.getCurrentPlayer();
        Collection<Card> mano = status.getCardsOfPlayer(currentPlayer);
        int colore = getColorePiuFrequente(mano);
        
        Card cms = status.getCartaMazzoScarti();
        int direction = status.getDirection();
        
        for(Card c : mano) {
            if(c.isJolly()) c.setColore(colore);
            try {
                callMethod(status, "handlePlayCardRequest", currentPlayer, c);
            } catch(InvalidRequestException ex) {
                /* Carta non valida */
            } catch(StatusChangedException ex) {
                /**
                 * Se si scarta un "Cambia giro" si verifica che il senso
                 * di marcia venga cambiato correttamente.
                 */
                if(!cms.equals(c) && c.isChangeDirection()) {
                    assertNotEquals(direction, status.getDirection());
                }
                
                /* Carta scartata con successo */
                assertEquals(c, status.getCartaMazzoScarti());
                assertEquals(mano.size() - 1, status.getCardsOfPlayer(currentPlayer).size());
                return true;
            } catch(PlayerWonException ex) {
                /* Il giocatore ha vinto */
                assertTrue(
                        "Il vincitore ha più di una carta in mano.",
                        status.getCardsOfPlayer(currentPlayer).isEmpty()
                );
                throw ex;
            } catch (Exception ex) {
                fail("Eccezione inaspettata: " + ex.getClass().getSimpleName());
            }
        }
        
        /* Non è possibile scartare una carta */
        return false;
    }
    
    
    /**
     * Fa pescare una o più carte al giocatore corrente.
     * 
     * Si verifica che sia pescato il numero corretto di carte.
     * 
     * @param status Stato della partita.
     */
    protected void pickCard(ServerMatchStatus status) {
        Player currentPlayer = status.getCurrentPlayer();
        int cards = status.getCardsOfPlayer(currentPlayer).size();
        int cardsToPick = status.getCardsToPick();
        
        try {
            callMethod(status, "handlePickCardRequest", currentPlayer);
        } catch(StatusChangedException ex) {
            /* Pescato una o più carte con successo */
            assertEquals(cards + cardsToPick, status.getCardsOfPlayer(currentPlayer).size());
        } catch(Exception ex) {
            fail("Eccezione inaspettata: " + ex.getClass().getSimpleName());
        }
    }
    
    
    /**
     * Tenta di far giocare una carta al giocatore corrente.
     * 
     * Se non ci riesce, esegue un "Pesca carta".
     * 
     * @param status Stato della partita 
     * 
     * @throws PlayerWonException quando il giocatore vince la partita.
     */
    protected void playCardElsePick(ServerMatchStatus status) throws PlayerWonException {
        /* Prova a scartare una carta */
        if(playCard(status)) return;
        
        /* Non è stata scartata alcuna carta, eseguo "Pesco carta" */
        pickCard(status);
    }
    
    
    /**
     * Ritorna il colore più frequente in una mano.
     * 
     * @param mano Mano di carte.
     * 
     * @return Colore più frequente nella mano, un qualsiasi colore se la mano
     * non ha un colore più frequente.
     */
    protected int getColorePiuFrequente(Collection<Card> mano) {
        /* Creazione contatori */
        HashMap<Integer, Integer> counters = new HashMap<>();
        mano.forEach((c) -> {
            int colore = c.getColore();
            if(!counters.containsKey(colore)) counters.put(colore, 0);
            counters.put(colore, counters.get(colore) + 1);
        });
        
        /* Rimozione colore neutro */
        if(counters.containsKey(Card.COLORE_NESSUNO)) counters.remove(Card.COLORE_NESSUNO);
        
        /* Recupero colore più frequente */
        if(counters.isEmpty()) counters.put(Card.COLORE_ROSSO, 1);
        int piuFrequente = Collections.max(counters.entrySet(), Map.Entry.comparingByValue()).getKey();
        
        /* Se è il colore neutro, ritorno il rosso */
        if(piuFrequente == Card.COLORE_NESSUNO) piuFrequente = Card.COLORE_ROSSO;
        return piuFrequente;
    }
    
}
