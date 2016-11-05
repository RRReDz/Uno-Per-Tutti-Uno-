/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import unoxtutti.gui.DebugFrame;

/**
 * Classe di ausilio per interfacciarsi con il frame di debug del software
 * @author Riccardo Rossi
 */
public class DebugHelper {
    private DebugFrame debugFrame = null;

    public DebugHelper() {
        if(Constants.DEBUG_MODE) {
            debugFrame = new DebugFrame();
            debugFrame.setVisible(true);
        }
    }
    
    /**
     * Scrive un messaggio nel frame di debug
     * @param msg 
     */
    public void logToDisplay(String msg) {
        if(Constants.DEBUG_MODE)
            debugFrame.writeToTextArea(msg);
    }
}
