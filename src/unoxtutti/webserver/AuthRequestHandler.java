/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.webserver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import unoxtutti.domain.Player;
import unoxtutti.domain.RegisteredPlayer;

/**
 *
 * @author picardi
 */
public class AuthRequestHandler extends WebRequestHandler {

    /**
     * Questo campo elenca i nomi delle richieste che venogon accettate.
     * Per ciascuna c'è in questa classe un corrispondente metodo.
     */
    private static final String[] accepted = new String[]{"verify", "createUser"};

    public AuthRequestHandler() {
    }

    /**
     * Il metodo canHandle è richiesto da WebRequestHandler
     * dice se una certa richiesta può essere gestita da questo Handler
     * per stabilirlo si basa sul NOME della richiesta
     * @param request Richiesta
     * @return true se la richiesta può essere soddisfatta, false altrimenti
     */
    @Override
    public boolean canHandle(WebRequest request) {
        for (String name : accepted) {
            if (name.equals(request.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Il metodo handle è richiesto da WebRequestHandler
     * gestisce effettivamente la richiesta, inoltrandola al metodo corrispondente
     * si occupa anche di fare il casting dei parametri Object ricevuti con la richiesta
     * nelle classi o tipi base richiesti dal metodo in questione.
     */
    @Override
    public Object handle(WebRequest request, ObjectOutputStream out) throws IOException {
        Object[] pars = request.getParameters();

        if (request.getName().equals(accepted[0])) { // verify
            String email = (String) pars[0];
            String password = (String) pars[1];
            Player pl = this.verify(email, password);
            out.writeObject(pl);
            return pl;
        } else if (request.getName().equals(accepted[1])) { // createUser
            String userName = (String) pars[0];
            String email = (String) pars[1];
            String password = (String) pars[2];
            boolean success = this.createUser(userName, email, password);
            out.writeObject(new Boolean(success));
            return success;
        }
        throw new WebRequestException("Handler " + this.getClass().getName() + " cannot handle Request " + request);
    }

    public boolean createUser(String userName, String email, String password) {
        String logMessage = "Tentativo di registrazione di " + userName + " (" + email + "): ";
        boolean success = false;
        try {
            boolean exists;
            exists = WebServer.getDBController().checkRegisteredPlayer(email);

            /* Se l'utente esiste già, la registrazione fallisce */
            if (exists) {
                logMessage += "ERR! Utente già registrato";
                return false;
            } else {
                /* Altrimenti crea l'utente */
                RegisteredPlayer reg = new RegisteredPlayer(0, userName, email, password);

                /* Salva l'utente nel Database */
                WebServer.getDBController().saveRegisteredPlayer(reg);
                logMessage += "OK!";
                success = true;
            }
        } catch (SQLException ex) {
            logMessage += "ERR! " + ex.getMessage();
            Logger.getLogger(AuthRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            WebServer.log(logMessage);
        }
        return success;
    }

    public Player verify(String email, String password) {
        String logMessage = "Tentativo di accesso di " + email + ": ";
        
        /* Cerca di recuperare coppia Username / Password */
        RegisteredPlayer reg = null;
        try {
            reg = WebServer.getDBController().loadRegisteredPlayer(email, password);
        } catch (SQLException ex) {
            logMessage += "ERR! " + ex.getMessage();
        }

        Player pl = null;
        if (reg == null) {
            /* Giocatore non trovato */
            logMessage += "ERR! Giocatore non trovato.";
        } else {
            /* Giocatore trovato con successo */
            pl = Player.createPlayer(reg);
            logMessage += "OK!";
        }
        WebServer.log(logMessage);
        return pl;
    }
}
