/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe con metodi utili per la gestione di mappe.
 * @author Davide
 */
public class MapUtils {
    /**
     * Ritorna una mappa ordinata in modo crescente in base al valore.
     * @param <K> Tipo di chiave
     * @param <V> Tipo di valore, deve essere Comparable
     * @param map Mappa di partenza
     * @return Nuova mappa ordinata in base al valore
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        Map.Entry::getValue, 
                        (e1, e2) -> e1, 
                        LinkedHashMap::new
                ));
    }
    
    
    /**
     * Ritorna una mappa ordinata in modo decrescente in base al valore.
     * @param <K> Tipo di chiave
     * @param <V> Tipo di valore, deve essere Comparable
     * @param map Mappa di partenza
     * @return Nuova mappa ordinata in base al valore
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueReverseOrder(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        Map.Entry::getValue, 
                        (e1, e2) -> e1, 
                        LinkedHashMap::new
                ));
    }
}
