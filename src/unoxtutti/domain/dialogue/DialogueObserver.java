/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoxtutti.domain.dialogue;

/**
 * Le classi che vogliono essere notificate dei cambi di stato all'interno
 * di una sequenza ogranizzata di messaggi (dialogue o dialogo) devono implementare
 * questa interfaccia e registrarsi presso un DialogueHandler.
 * @author picardi
 */
public interface DialogueObserver {
	public void updateDialogueStateChanged(DialogueHandler source);
}
