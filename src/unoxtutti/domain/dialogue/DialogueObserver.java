/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
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
