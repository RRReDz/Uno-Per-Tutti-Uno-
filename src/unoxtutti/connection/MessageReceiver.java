/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.connection;

/**
 * L'interfaccia MessageReceiver va a definire, insieme alla P2PConnection, un
 * pattern Observer relativo alla ricezione di messaggi. Le classi che vogliono
 * osservare i messaggi in arrivo devono implementare questa interfaccia e le loro
 * istanze devono registrarsi presso la P2PConnection tramite il metodo addMessageReceivedObserver,
 * specificando a quale tipo di messaggio sono interessate. 
 * Ogni tipo di messaggio (identificato tramite il <em>name</em> dell'oggetto P2PMessage)
 * ha una lista di osservatori separata. Un oggetto puo' registrarsi pi&ugrave; volte
 * per pi&ugrave; tipi di messaggi, cos&igrave; come pu&ograve; smettere di osservare
 * un certo tipo di messaggio tramite il metodo removeMessageReceivedObserver di 
 * P2PConnection.
 * @author picardi
 */
public interface MessageReceiver {
	/** Questo metodo viene invocato dal soggetto osservato (un'istanza di P2PConnection)
	 * quando arriva un messaggio del tipo a cui il MessageReceiver si &egrave; dichiarato
	 * interessato.
	 * @param msg Il messaggio ricevuto.
	 */
	public void updateMessageReceived(P2PMessage msg);
}
