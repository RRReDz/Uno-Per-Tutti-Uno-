/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Rappresenta un mazzo di carte.
 * @author Davide
 */
public class Deck {
    protected LinkedList<Card> cards;
    
    public Deck() {
        cards = new LinkedList();
        
        addCards();
    }
    
    /**
     * Aggiunge le carte standard al mazzo di pesca e poi le mescola.
     */
    private void addCards() {
        for(int colore = 1; colore <= 4; colore++) {
            /* Aggiunta carte base */
            cards.addFirst(new Card(Card.CARTA_BASE, 0, colore));
            for(int numero = 1; numero <= 9; numero++) {
                cards.addFirst(new Card(Card.CARTA_BASE, numero, colore));
                cards.addFirst(new Card(Card.CARTA_BASE, numero, colore));
            }
            
            /* Aggiunta carte azione */
            cards.addFirst(new Card(Card.CARTA_AZIONE, Card.AZIONE_CAMBIO_GIRO, colore));
            cards.addFirst(new Card(Card.CARTA_AZIONE, Card.AZIONE_CAMBIO_GIRO, colore));
            cards.addFirst(new Card(Card.CARTA_AZIONE, Card.AZIONE_PESCA_DUE, colore));
            cards.addFirst(new Card(Card.CARTA_AZIONE, Card.AZIONE_PESCA_DUE, colore));
            cards.addFirst(new Card(Card.CARTA_AZIONE, Card.AZIONE_SALTA_TURNO, colore));
            cards.addFirst(new Card(Card.CARTA_AZIONE, Card.AZIONE_SALTA_TURNO, colore));
        }
        
        /* Aggiunte carte jolly */
        for(int i = 0; i < 4; i++) {
            cards.addFirst(new Card(Card.CARTA_JOLLY, Card.JOLLY_CAMBIA_COLORE, Card.COLORE_NESSUNO));
            cards.addFirst(new Card(Card.CARTA_JOLLY, Card.JOLLY_PESCA_QUATTRO, Card.COLORE_NESSUNO));
        }
        
        shuffle();
    }
    
    /**
     * Mescola casualmente le carte del mazzo di pesca.
     */
    private void shuffle() {
        Collections.shuffle((List<?>) cards);
    }
    
    /**
     * Pesca una carta, la ritorna e la toglie dal mazzo.
     * @return Carta pescata
     */
    public Card pescaCarta() {
        /* Se la lista è vuota, aggiungo delle carte */
        if(cards.isEmpty()) {
            addCards();
        }
        return cards.removeFirst();
    }
}
