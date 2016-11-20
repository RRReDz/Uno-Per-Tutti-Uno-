/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.webserver;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author picardi
 */
public class DBSetup {

    public static void main(String[] args) {
        DBController dbc = new DBController();
        try {
            dbc.connect();
            try {
                dbc.resetDB();
            } finally {
                dbc.disconnect();
            }
        } catch (SQLException exc) {
            Logger.getLogger(DBSetup.class.getName()).log(Level.SEVERE, null, exc);
        }
    }
}
