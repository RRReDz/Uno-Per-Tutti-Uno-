/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import unoxtutti.gui.DebugGUI;

/**
 * Classe di ausilio per interfacciarsi con il frame di debug del software
 * @author Riccardo Rossi
 */
public class DebugHelper {
    private static DebugGUI debugFrame = null;
    
    /**
     * Avvia la console per il debug delle azioni del client.
     * Funge come un metodo getInstance() di una classe singleton.
     */
    synchronized public static void startDebugConsole() {
        if(debugFrame == null && Constants.DEBUG_MODE) {
            debugFrame = new DebugGUI();
            debugFrame.setVisible(true);
        }
    }
    
    /**
     * Scrive un messaggio nel frame di debug
     * @param msg 
     */
    synchronized public static void log(String msg) {
        if(debugFrame != null) {
            debugFrame.appendMessageToConsole(msg);
        }
    }
}
