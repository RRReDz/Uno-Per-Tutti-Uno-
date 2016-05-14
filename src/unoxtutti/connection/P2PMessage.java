/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.connection;

import java.io.Serializable;

/**
 * I messaggi scambiati fra client e server sono di tipo P2PMessage.
 * Ogni P2PMessage ha un nome e dei parametri, rappresentati con 
 * un'array di Object. Due messaggi particolari, Hello e Bye, possono
 * essere creati tramite factory methods. Hello viene usato nell'handshake
 * iniziale di una connessione, mentre Bye viene usato per chiudere la 
 * connessione stessa.
 * @author picardi
 */
public class P2PMessage implements Serializable {
	private final static String BYE_MSG = "___BYE___";
	private final static String HELLO_MSG = "___HELLO___";
	protected String name;
	private Object[] parameters;
	private P2PConnection senderConnection;
	
	private P2PMessage() {
	}
	
	public void setSenderConnection(P2PConnection conn) {
		senderConnection = conn;
	}
	
	public P2PConnection getSenderConnection() {
		return senderConnection;
	}
	
	/**
	 * Costruisce un nuovo messaggio del tipo specificato.
	 * @param name Il nome (o tipo) del messaggio.
	 */
	public P2PMessage(String name) {
		this.name = name;
		parameters = new Object[0];
	}
	
	/** Imposta i parametri del messaggio.
	 * @param pars Un array contenente i parametri del messaggio.
	 */
	public void setParameters(Object[] pars) {
		this.parameters = pars;
	}

	/** Stabilisce se il messaggio attuale &egrave; un messaggio Bye.
	 * @return true se si tratta di un messaggio Bye, false altrimenti.
	 */
	public boolean isByeMessage() {
		return this.getName().equals(BYE_MSG);
	}

	/** Stabilisce se il messaggio attuale &egrave; un messaggio Hello.
	 * @return true se si tratta di un messaggio Hello, false altrimenti.
	 */	
	public boolean isHelloMessage() {
		return this.getName().equals(P2PMessage.HELLO_MSG);
	}
	
	/** Factory method per creare un messaggio Bye.
	 * 
	 * @return il messaggio creato
	 */
	public static P2PMessage createByeMessage() {
		P2PMessage msg = new P2PMessage();
		msg.name = BYE_MSG;
		return msg;
	}

	/** Factory method per creare un messaggio Hello.
	 * 
	 * @return il messaggio creato
	 */
	public static P2PMessage createHelloMessage() {
		P2PMessage msg = new P2PMessage();
		msg.name = P2PMessage.HELLO_MSG;
		return msg;
	}	
	
	/**
	 * @return l'i-esimo parametro di questo messaggio
	 */
	
	public Object getParameter(int i) {
		if (i < 0 || i >= parameters.length) return null;
		return  parameters[i];
	}
	
	/**
	 * @return il numero di parametri di questo messaggio
	 */
	public int getParametersCount() {
		return parameters.length;
	}

	/**
	 * @return nome/tipo di questo messaggio
	 */
	public String getName() {
		return name;
	}
}
