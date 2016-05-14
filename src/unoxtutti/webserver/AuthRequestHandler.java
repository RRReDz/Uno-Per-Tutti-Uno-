/*
 * Questa classe gestisce le richieste relative all'autenticazione.
 * Estende WebRequestHandler per potersi registrare presso il finto
 * WebServer come gestore di richieste.
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

	/* Questo campo elenca i nomi delle richieste che venogon accettate.
	 * Per ciascuna c'e' in questa classe un corrispondente metodo.
	*/
	private static final String[] accepted = new String[]{"verify", "createUser"}; 
	
	public AuthRequestHandler() {
	}

	/* Il metodo canHandle e' richiesto da WebRequestHandler
	 * dice se una certa richiesta puo' essere gestita da questo Handler
	 * per stabilirlo si basa sul NOME della richiesta
	 */
	@Override
	public boolean canHandle(WebRequest request) {
		for(String name: accepted) {
			if (name.equals(request.getName())) {
				return true;
			}
		}
		return false;
	}

	/* Il metodo handle e' richiesto da WebRequestHandler
	 * gestisce effettivamente la richiesta, inoltrandola al metodo corrispondente
	 * si occupa anche di fare il casting dei parametri Object ricevuti con la richiesta
	 * nelle classi o tipi base richiesti dal metodo in questione.
	 */	
	@Override
	public Object handle(WebRequest request, ObjectOutputStream out) throws IOException {
		Object[] pars = request.getParameters();
		
		if (request.getName().equals(accepted[0])) { // verify
			String email = (String)pars[0];
			String password = (String)pars[1];
			Player pl = this.verify(email, password);
			out.writeObject(pl);
			return pl;
		} else if (request.getName().equals(accepted[1])) { // createUser
			String userName = (String)pars[0];
			String email = (String)pars[1];
			String password = (String)pars[2];
			boolean success = this.createUser(userName, email, password);
			out.writeObject(new Boolean(success));
			return success;
		}
		throw new WebRequestException("Handler " + this.getClass().getName() + " cannot handle Request " + request);
	}
	
	public boolean createUser(String userName, String email, String password) {
		try {
		boolean exists;
			exists = WebServer.getDBController().checkRegisteredPlayer(email);

		
		// if it exists, fail
		if (exists) return false;
		
		// else create new user
		RegisteredPlayer reg = new RegisteredPlayer(0, userName, email, password);
		
			// then save on DB
			WebServer.getDBController().saveRegisteredPlayer(reg);
		} catch (SQLException ex) {
						Logger.getLogger(AuthRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return true; // success
	}
	
	public Player verify(String email, String password) {
		
		// check for email,pwd pair on DB by retrieving the corresponding registered player
		RegisteredPlayer reg = null;
		try {
			reg = WebServer.getDBController().loadRegisteredPlayer(email, password);
		} catch (SQLException ex) {
		}
		
		// if it DOES NOT exist then fail
		if (reg == null) return null;
		
		// otherwise create corresponding Player and return it
		Player pl = Player.createPlayer(reg);
		return pl;
	}
}
