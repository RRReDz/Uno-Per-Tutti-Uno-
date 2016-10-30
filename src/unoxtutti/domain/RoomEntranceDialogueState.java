/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.domain;

import unoxtutti.connection.P2PMessage;
import unoxtutti.domain.Room;
import unoxtutti.domain.dialogue.DialogueState;

/**
 * Gli stati del dialogo per entrare in una stanza:
 * <ul>
 * <li>BEFORE_REQUEST: la richiesta non &egrave; ancora stata inviata, o c'&egrave; stato
 * un problema nell'invio.
 * <li>REQUESTED: &egrave; stata inviata la richiesta di ingresso
 * <li>ADMITTED: la richiesta ha avuto risposta positiva
 * <li>REJECTED: la richiesta ha avuto risposta negativa
 * </ul>
 *
 * @author picardi
 */
public enum RoomEntranceDialogueState implements DialogueState<RoomEntranceDialogueState> {

	BEFORE_REQUEST, REQUESTED, ADMITTED, REJECTED;

	@Override
	public RoomEntranceDialogueState nextState(P2PMessage msg) {
		String msgName = msg.getName();
		switch (this) {
			case REQUESTED:
				if (msgName.equals(Room.roomEntranceReplyMsg)) {
					boolean accepted = (Boolean) msg.getParameter(0);
					if (accepted) {
						return ADMITTED;
					} else {
						return REJECTED;
					}
				}
			case ADMITTED:
			case REJECTED:
			case BEFORE_REQUEST:
			default:
				return this;
		}
	}

}
