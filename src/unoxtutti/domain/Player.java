/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Rappresenta i giocatori autenticati presenti nel sistema.
 * @author picardi
 */
public class Player implements Serializable {
	private String name;
	private int id;
	
	private Player() {};
	
	/**
	 * Crea un nuovo giocatore a partire da una scheda di registrazione
	 * @param reg la scheda di registrazione
	 * @return il giocatore creato.
	 */
	public static Player createPlayer(RegisteredPlayer reg) {
		Player pl = new Player();
		pl.id = reg.getId();
		pl.name = reg.getUserName();
		return pl;
	}
	
	/**
	 * 
	 * @return  il nome del giocatore da mostrare agli altri giocatori
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return una rappresentazione testuale del giocatore (corrisponde al nome)
	 */
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + Objects.hashCode(this.name);
		hash = 61 * hash + this.id;
		return hash;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Player)) return false;
		return (((Player)o).id == id && ((Player)o).name.equals(name));
	}
}
