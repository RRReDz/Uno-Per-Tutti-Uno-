/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti;

import java.net.InetAddress;
import java.net.UnknownHostException;
import unoxtutti.domain.Player;
import unoxtutti.webclient.WebClientConnection;

/**
 * Controller GRASP per l'UC "Autenticarsi".
 * E' un singleton, quindi l'unica istanza di questa
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
	 * @return true se la registrazione e' andata a buon fine, false altrimenti
	 */
	public boolean registra(String userName, String email, String password) {
		return webCliConn.createUser(userName, email, password);
	}
	
	/** Operazione utente definita nei contratti
	 * @param email L'indirizzo email fornito al momento della registrazione
	 * @param password La password scelta al momento dell'autenticazione
	 * @return l'oggetto Player relativo al giocatore che si e' autenticato
	 */
	public Player accedi(String email, String password) {
		thePlayer = webCliConn.verify(email, password);
		return getPlayer();
	}
	
	/**
	 * Inizializza il controller connettendolo al Web Server Simulato. 
	 * Indirizzo e porta sono definiti come variabili statiche di UnoXTutti
	 * @return true se la connessione e' stata possibile, false altrimenti
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
	 * null se l'autenticazione non e' ancora avvenuta o e' fallita.
	 */
	public Player getPlayer() {
		return thePlayer;
	}
	
}
