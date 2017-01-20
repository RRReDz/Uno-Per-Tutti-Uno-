/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.domain;

import java.io.Serializable;

/**
 * Rappresenta una carta di un mazzo.
 * @author Davide
 */
public class Card implements Serializable, Comparable {
    /**
     * Tipi di carte.
     */
    public static final int CARTA_BASE = 0;
    public static final int CARTA_AZIONE = 1;
    public static final int CARTA_JOLLY = 2;
    
    /**
     * Colori
     */
    public static final int COLORE_NESSUNO = 0;
    public static final int COLORE_ROSSO = 1;
    public static final int COLORE_BLU = 2;
    public static final int COLORE_GIALLO = 3;
    public static final int COLORE_VERDE = 4;
    
    /**
     * Tipi di carte azione
     */
    public static final int AZIONE_PESCA_DUE = 0;
    public static final int AZIONE_CAMBIO_GIRO = 1;
    public static final int AZIONE_SALTA_TURNO = 2;
    
    /**
     * Tipi di jolly
     */
    public static final int JOLLY_CAMBIA_COLORE = 0;
    public static final int JOLLY_PESCA_QUATTRO = 1;
    
    /**
     * Indica il tipo della carta.
     */
    private final int tipo;
    
    /**
     * Indica il numero o il simbolo della carta.
     */
    private final int dettaglio;
    
    /**
     * Colore della carta.
     */
    private final int colore;
    
    /**
     * Crea una carta
     * @param tipo Tipo della carta
     * @param dettaglio Numero o simbolo della carta
     * @param colore Colore della carta
     */
    public Card(int tipo, int dettaglio, int colore) {
        /* Controllo validità tipo */
        if(tipo != CARTA_BASE && tipo != CARTA_AZIONE && tipo != CARTA_JOLLY) {
            throw new IllegalArgumentException("Tipo di carta non valido.");
        }
        this.tipo = tipo;
        
        /**
         * Controllo validità dettaglio:
         * - le carte base sono numeri da 0 a 9
         */
        switch(tipo) {
            case CARTA_BASE:
                if(dettaglio < 0 || dettaglio > 9) {
                    throw new IllegalArgumentException("Le carte base devono essere numeri da 0 a 9.");
                }
                break;
            case CARTA_AZIONE:
                if(dettaglio != AZIONE_PESCA_DUE && dettaglio != AZIONE_CAMBIO_GIRO
                        && dettaglio != AZIONE_SALTA_TURNO) {
                    throw new IllegalArgumentException("Le carte azione devono essere di tipo \"Pesca due\","
                            + "\"Cambio giro\" oppure \"Salta turno\".");
                }
                break;
            case CARTA_JOLLY:
                if(dettaglio != JOLLY_CAMBIA_COLORE && dettaglio != JOLLY_PESCA_QUATTRO) {
                    throw new IllegalArgumentException("I jolly devono essere normali o di tipo \"Pesca quattro\".");
                }
                break;
        }
        this.dettaglio = dettaglio;
        
        /* Controllo validità colore */
        if(colore != COLORE_NESSUNO && colore != COLORE_ROSSO && colore != COLORE_BLU
                && colore != COLORE_GIALLO && colore != COLORE_VERDE) {
            throw new IllegalArgumentException("Colore non valido.");
        }
        if(colore == COLORE_NESSUNO && tipo != CARTA_JOLLY) {
            throw new IllegalArgumentException("Solo le carte jolly sono incolore.");
        }
        this.colore = colore;
    }
    
    /**
     * Scorciatoia per la creazione di una carta senza colore.
     * @param tipo Tipo di carta
     * @param dettaglio Numero o simbolo della carta.
     */
    public Card(int tipo, int dettaglio) {
        this(tipo, dettaglio, Card.COLORE_NESSUNO);
    }

    /**
     * Restituisce il tipo della carta.
     * @return Tipo della carta.
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Restituisce il dettaglio della carta.
     * @return Dettaglio della carta.
     */
    public int getDettaglio() {
        return dettaglio;
    }

    /**
     * Restituisce il colore della carta.
     * @return Colore della carta.
     */
    public int getColore() {
        return colore;
    }
    
    /**
     * Restituisce il valore della carta per l'inizializzazione dei turni.
     * @return Valore della carta
     */
    public int getCardValue() {
        if(tipo == CARTA_AZIONE || tipo == CARTA_JOLLY) {
            return 0;
        } else {
            return dettaglio;
        }
    }
    
    /**
     * Ritorna il tipo di una carta dettaglio sotto forma di stringa.
     * @param tipo Tipo
     * @param dettaglio Dettaglio
     * @return Descrizione della carta
     */
    public static String getDetailName(int tipo, int dettaglio) {
        switch(tipo) {
            case CARTA_BASE:
                return "" + dettaglio;
            case CARTA_AZIONE:
                switch (dettaglio) {
                    case AZIONE_CAMBIO_GIRO:
                        return "Cambio giro";
                    case AZIONE_PESCA_DUE:
                        return "Pesca due";
                    case AZIONE_SALTA_TURNO:
                        return "Salta turno";
                    default:
                        throw new IllegalArgumentException("Tipo di carta azione non valido.");
                }
            case CARTA_JOLLY:
                switch (dettaglio) {
                    case JOLLY_CAMBIA_COLORE:
                        return "Jolly";
                    case JOLLY_PESCA_QUATTRO:
                        return "Jolly pesca quattro";
                    default:
                        throw new IllegalArgumentException("Tipo di jolly non valido.");
                }
            default:
                throw new IllegalArgumentException("Tipo di carta non valido.");
        }
    }
    
    /**
     * Ritorna il nome di un colore.
     * @param color Colore
     * @return Nome del colore.
     */
    public static String getColorName(int color) {
        switch(color) {
            case COLORE_NESSUNO:
                return "Nessuno";
            case COLORE_ROSSO:
                return "Rosso";
            case COLORE_BLU:
                return "Blu";
            case COLORE_VERDE:
                return "Verde";
            case COLORE_GIALLO:
                return "Giallo";
            default:
                throw new IllegalArgumentException("Colore non valido.");
        }
    }
    
    /**
     * Verifica se due carte sono uguali (stesso tipo, dettaglio, colore).
     * @param obj Carta con cui confrontare quella istanziata.
     * @return <code>true</code> se le due carte sono uguali,
     * <code>false</code> altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Card other = (Card) obj;
        if (this.tipo != other.tipo || this.dettaglio != other.dettaglio
                || this.colore != other.colore) {
            return false;
        }
        return true;
    }
    
    /**
     * Ritorna una stringa con i dettagli della carta
     * @return Descrizione della carta
     */
    @Override
    public String toString() {
        switch(tipo) {
            case CARTA_BASE:
                /* Numero colorato */
                return getDetailName(tipo, dettaglio) + " " + getColorName(colore);
            case CARTA_AZIONE:
                /* Carta azione colorata */
                return getDetailName(tipo, dettaglio) + " " + getColorName(colore);
            case CARTA_JOLLY:
                /* Jolly */
                return getDetailName(tipo, dettaglio);
        }
        throw new IllegalStateException("Tipo non riconosciuto.");
    }

    @Override
    public int compareTo(Object o) {
        /* Verifica validità argomento */
        if(o == null) {
            throw new IllegalArgumentException("L'argomento non può essere nullo.");
        }
        if(getClass() != o.getClass()) {
            throw new IllegalArgumentException(
                    "L'oggetto argomento deve essere di tipo \"Card\", \""
                            + o.getClass() + "\" ricevuto."
            );
        }
        
        /* Ritorno la differenza di valore tra le due carte */
        final Card other = (Card) o;
        return getCardValue() - other.getCardValue();
    }

    
    /**
     * Indica se la carta è un Jolly Pesca Quattro.
     * @return <code>true</code> se la carta è un Jolly Pesca Quattro,
     * <code>false</code> altrimenti.
     */
    public boolean isJollyPescaQuattro() {
        return tipo == CARTA_JOLLY && dettaglio == JOLLY_PESCA_QUATTRO;
    }
}
