/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Libreria di metodi utili per date e scorrere del tempo.
 * @author Davide
 */
public class TimeUtils {
    
    /**
     * Restituisce l'ora corrente nel formato desiderato
     * @param format Formato della stringa desiderata
     * @return Ora corrente nel formato desiderato
     */
    public static String getCurrentTimeStamp(String format) {
        SimpleDateFormat sdfDate = new SimpleDateFormat(format);
        return sdfDate.format(new Date());
    }
}
