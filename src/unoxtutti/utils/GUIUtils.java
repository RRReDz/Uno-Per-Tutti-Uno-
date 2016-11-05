/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import unoxtutti.gui.AutenticarsiGUI;

/**
 * Libreria di metodi utili per azioni sull'interfaccia grafica.
 * @author Davide
 */
public class GUIUtils {
    
    /**
     * Prova ad impostare WebLaF come Look&Feel di Java.
     * Si ottiene un risultato positivo solo quando la libreria è presente,
     * altrimenti non si fa nulla e si lascia il Look&Feel già impostato.
     * 
     * @see http://weblookandfeel.com/
     */
    public static void InstallLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(AutenticarsiGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
