/*
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
    
    
    /**
     * Imposta la posizione di un dialog al centro di una finestra
     * @param dialog Finestra di dialogo
     * @param window Finestra in cui mettere il dialog
     */
    public static void CenterDialogInsideWindow(JDialog dialog, JFrame window) {
        /* Recupero coordinate */
        Rectangle diarect = dialog.getBounds();
        Rectangle mainrect = window.getBounds();
        
        /* Centramento */
        dialog.setBounds(
            (int) (mainrect.getCenterX() - diarect.getWidth() / 2),
            (int) (mainrect.getCenterY() - diarect.getHeight() / 2),
            (int) diarect.getWidth(),
            (int) diarect.getHeight()
        );
    }
    
    
    /**
     * Visualizza a schermo un errore utilizzando le
     * librerie Java predefinite (JOptionPane).
     * @param parentComponent Componente padre (frame in cui centrare l'errore)
     * @param errorMessage Messaggio di errore
     * @param title Titolo del messaggio
     */
    public static void ShowErrorMessage(Component parentComponent, String errorMessage, String title) {
        JOptionPane.showMessageDialog(
                    parentComponent,
                    errorMessage,
                    title,
                    JOptionPane.ERROR_MESSAGE
            );
    }
}
