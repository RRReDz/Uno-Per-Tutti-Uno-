/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.domain;

/**
 * Rappresenta la scheda di registrazione di un giocatore.
 * @author picardi
 */
public class RegisteredPlayer {
	private int id;
	protected final String userName;
	protected final String password;
	protected final String email;

	/**
	 * @return L'id con cui il giocatore &egrave; registrato
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Crea una nuova scheda di registrazione
	 * @param id l'id del giocatore nel DB, 0 se non &egrave; presente
	 * @param userName Il nome del giocatore
	 * @param email L'email da usare per l'autenticazione
	 * @param password La password da usare per l'autenticazione
	 */
	public RegisteredPlayer(int id, String userName, String email, String password) {
		this.id = id;
		this.userName = userName;
		this.password = password;
		this.email = email;
	}

	/**
	 * @return il nome del giocatore
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return la password del giocatore
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return la email del giocatore
	 */
	public String getEmail() {
		return email;
	}
}
