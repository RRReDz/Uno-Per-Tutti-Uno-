/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti;

import java.net.InetAddress;
import java.net.UnknownHostException;
import unoxtutti.domain.Player;
import unoxtutti.webclient.WebClientConnection;

/**
 * Controller GRASP per l'UC "Autenticarsi".
 * È un singleton, quindi l'unica istanza di questa
 * classe viene ottenuta tramite il metodo statico getInstance
 * @author picardi
 */
public class AutenticarsiController {
	
	private static AutenticarsiController singleInstance;
	private Player thePlayer;
	private WebClientConnection webCliConn;
	
	private AutenticarsiController() {}
	
	/**
	 * 
	 * @return L'unica istanza di AutenticarsiController
	 */
	public static AutenticarsiController getInstance() {
		if (singleInstance == null) singleInstance = new AutenticarsiController();
		return singleInstance;
	}
	
	/**
	 * Operazione utente definita nei contratti
	 * @param userName Il nome utente da usare nel gioco
	 * @param email L'indirizzo email da usare per l'autenticazione
	 * @param password La password da usare per l'autenticazione
	 * @return true se la registrazione è andata a buon fine, false altrimenti
	 */
	public boolean registra(String userName, String email, String password) {
		return webCliConn.createUser(userName, email, password);
	}
	
	/** Operazione utente definita nei contratti
	 * @param email L'indirizzo email fornito al momento della registrazione
	 * @param password La password scelta al momento dell'autenticazione
	 * @return l'oggetto Player relativo al giocatore che si è autenticato
	 */
	public Player accedi(String email, String password) {
		thePlayer = webCliConn.verify(email, password);
		return getPlayer();
	}
	
	/**
	 * Inizializza il controller connettendolo al Web Server Simulato. 
	 * Indirizzo e porta sono definiti come variabili statiche di UnoXTutti
	 * @return true se la connessione è stata possibile, false altrimenti
	 */
	boolean initialize() {
		try {
			webCliConn = new WebClientConnection(InetAddress.getByName(UnoXTutti.WEB_ADDRESS), UnoXTutti.WEB_PORT);
		} catch (UnknownHostException ex) {
			System.out.println("Host " + UnoXTutti.WEB_ADDRESS + " sconosciuto.");
			return false;
		}
		return true;
	}

	/**
	 * @return l'oggetto Player che rappresenta il giocatore autenticato, 
	 * null se l'autenticazione non è ancora avvenuta o è fallita.
	 */
	public Player getPlayer() {
		return thePlayer;
	}
	
}
