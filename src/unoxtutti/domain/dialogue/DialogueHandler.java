/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.domain.dialogue;

import java.util.ArrayList;
import unoxtutti.connection.MessageReceiver;
import unoxtutti.connection.P2PMessage;

/**
 * La classe DialogueHandler &egrave; la classe base di tutti le classi che gestiscono
 * una sequenza organizzata di scambi di messaggi. I DialogueHandler sono dei MessageReceiver,
 * quindi possono essere registrati presso una P2PConnection per essere notificati dell'arrivo
 * di quei messaggi che costituiscono il dialogo (in assenza di tale registrazione, non 
 * riceveranno le notifiche e pertanto non potranno evolvere il proprio stato interno). 
 * Le sottoclassi di DialogueHandler devono:
 * <ul>
 * <li>indicare una classe T che rappresenti le fasi, o stati, del dialogo. Le istanze di T sono gli stati possibili.
 * <li>implementare il metodo evolveState che determina i passaggi di stato in base ai messaggi
 * ricevuti.
 * </ul>
 * @author picardi
 * @param <T> La classe che rappresenta gli stati, o fasi, del dialogo.
 */
public abstract class DialogueHandler<T> implements MessageReceiver {

	private T currentState;
	private P2PMessage trigger;
	private final ArrayList<DialogueObserver> observers;

	/**
	 * Costruisce un DialogueHandler con un dato stato iniziale. 
	 * @param initialState lo stato iniziale di questo DialogueHandler
	 */
	public DialogueHandler(T initialState) {
		currentState = initialState;
		observers = new ArrayList<>();
	}

	/** 
	 * Modifica lo stato corrente del DialogueHandler (dovrebbe essere chiamato
	 * solo dalle sottoclassi, nel contesto di evolveState).
	 * @param state Il nuovo stato.
	 */
	protected void setState(T state) {
		currentState = state;
	}

	/**
	 * 
	 * @return Lo stato corrente del DialogueHandler
	 */
	public T getState() {
		return currentState;
	}

	/** 
	 * Il messaggio che &egrave; stato causa scatenante dell'ultimo
	 * cambio di stato.
	 * @return Il messaggio che ha causato l'ultimo cambio di stato, o null
	 * se il DialogueHandler &egrave; nel suo stato iniziale.
	 */
	public P2PMessage getStateChangeTrigger() {
		return trigger;
	}

	/** 
	 * Registra un observer per essere notificato quando avviene un cambio di stato.
	 * @param obs L'observer da registrare.
	 */
	public void addStateChangeObserver(DialogueObserver obs) {
		observers.add(obs);
	}

	/**
	 * Rimuove un observer in modo che non venga pi&ugrave; notificato del cambio
	 * di stato.
	 * @param obs 
	 */
	public void removeStateChangeObserver(DialogueObserver obs) {
		observers.remove(obs);
	}

	/** Viene chiamata dalla P2PConnection quando riceve un messaggio per cui questo
	 * oggetto si &egrave; registrato. Chiama evolveState per determinare l'eventuale
	 * cambiamento di stato, e notifyStateChange per avvertire gli observer se questo
	 * cambiamento &egrave; avvenuto.
	 * @param msg Il messaggio ricevuto.
	 */
	public synchronized void updateMessageReceived(P2PMessage msg) {
		boolean stateChanged = evolveState(msg);
		if (stateChanged) {
			trigger = msg;
			notifyStateChange();
		}
	}
	
	
	/** Avverte gli observer di un cambio di stato.
	 * 
	 */
	protected synchronized void notifyStateChange() {
		for (DialogueObserver obs : observers) {
			obs.updateDialogueStateChanged(this);
		}
	}
	
	/** Determina l'eventuale cambiamento di stato a partire
	 * dalla ricezione di un messaggio.
	 * @param msg Il messaggio ricevuto.
	 * @return true se &egrave; avvenuto un cambiamento di stato, false altrimenti.
	 */
	protected abstract boolean evolveState(P2PMessage msg);
}
