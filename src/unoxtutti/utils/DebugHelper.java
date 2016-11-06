/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import java.awt.EventQueue;
import java.util.Timer;
import unoxtutti.configuration.DebugConfig;
import unoxtutti.gui.DebugGUI;

/**
 * Classe di ausilio per interfacciarsi con il frame di debug del software
 * @author Riccardo Rossi
 */
public class DebugHelper {
    /**
     * Finestra di Debug
     */
    private static DebugGUI debugFrame = null;
    
    /**
     * Avvia la console per il debug delle azioni del client.
     * Funge come un metodo getInstance() di una classe singleton.
     */
    synchronized public static void startDebugConsole() {
        if(debugFrame == null && DebugConfig.DEBUG_MODE) {
            debugFrame = new DebugGUI();
            debugFrame.setVisible(true);
        }
    }
    
    /**
     * Chiude la finestra di Debug
     */
    synchronized public static void stopDebugConsole() {
        stopDebugConsole(0);
    }
    
    /**
     * Chiude la finestra di Debug dopo un certo lasso di tempo
     * @param seconds Secondi prima della chiusura della finestra
     */
    synchronized public static void stopDebugConsole(int seconds) {
        if(seconds > 0) {
            DebugHelper.log("Questa finestra verrà chiusa tra " + seconds + " secondi.");
        }
        
        if(debugFrame != null) {
            Timer t = new Timer();
            t.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        DebugHelper.log("Chiusura della finestra in corso...");
                        /* Richiamo la chiusura della finestra dal thread principale */
                        EventQueue.invokeLater(() -> {
                            debugFrame.setVisible(false);
                            debugFrame.dispose();
                        });
                        t.cancel();
                    }
                },
                seconds * 1000
            );
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
    
    /**
     * Aggiunge una stringa al titolo del frame di debug
     * @param msg stringa da aggiungere
     */
    synchronized public static void appendToDebugConsoleTitle(String msg) {
        if(debugFrame != null) {
            debugFrame.setTitle(debugFrame.getTitle() + " - " + msg);
        }
    }
}
