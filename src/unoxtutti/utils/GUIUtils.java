/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.utils;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
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
    public static void installLookAndFeel() {
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
    public static void centerDialogInsideWindow(JDialog dialog, JFrame window) {
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
     * Visualizza a schermo un errore.
     * @param parentComponent Componente padre
     * @param errorMessage Messaggio di errore
     * @param title Titolo del messaggio
     */
    public static void showErrorMessage(Component parentComponent, String errorMessage, String title) {
        showMessageDialog(parentComponent, errorMessage, title, JOptionPane.ERROR_MESSAGE, null);
    }
    
    
    /**
     * Visualizza a schermo un errore.
     * @param parentComponent Componente padre
     * @param errorMessage Messaggio di errore
     */
    public static void showErrorMessage(Component parentComponent, String errorMessage) {
        GUIUtils.showErrorMessage(parentComponent, errorMessage, "Errore");
    }
    
    
    /**
     * Mostra un'eccezione inaspettata all'utente (per debug).
     * @param e Eccezione
     * @param parentComponent Finestra in cui centrare il popup
     */
    public static void showException(Exception e, Component parentComponent) {
        String exceptionMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
        DebugHelper.appendToDebugConsoleTitle(exceptionMessage);
        GUIUtils.showErrorMessage(parentComponent, exceptionMessage);
    }
    
    
    /**
     * Chiede all'utente una scelta tra due o più opzioni.
     * @param parentComponent Componente padre
     * @param question Testo della domanda
     * @param title Titolo del Dialog
     * @return Risposta dell'utente
     */
    public static int askYesOrNoQuestion(Component parentComponent, String question, String title) {
        return askQuestion(
                parentComponent,
                question,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, 
                null,
                null,
                null
        );
    }
    
    
    /**
     * Pone all'utente una domanda
     * @param parentComponent Componente padre
     * @param question Testo della domanda
     * @param title Titolo del dialog
     * @param optionType Tipo di domanda (YES_NO, YES_NO_CANCEL, ecc.)
     * @param messageType Tipo di messaggio
     * @param icon Icona del Dialog
     * @param options Possibili opzioni
     * @param defaultAnswer Risposta predefinita
     * @return Risposta dell'utente
     */
    public static int askQuestion(
            Component parentComponent, String question, String title, int optionType, int messageType,
            Icon icon, Object[] options, Object[] defaultAnswer
    ) {
        return JOptionPane.showOptionDialog(
                parentComponent,
                question,
                title,
                optionType,
                messageType,
                icon,
                options,
                defaultAnswer
        );
    }
    
    
    /**
     * Mostra un dialogo con un messaggio
     * @param parentComponent Componente in cui centrare il dialog
     * @param message Messaggio da visualizzare
     * @param title Titolo del dialog
     * @param messageType Tipo di messaggio
     * @param icon Icona
     */
    public static void showMessageDialog(
            Component parentComponent, String message, String title, int messageType, Icon icon
    ) {
        JOptionPane.showMessageDialog(parentComponent, message, title, messageType, icon);
    }

    
    /**
     * Mostra un dialogo per comunicare all'utente una bella notizia!
     * @param parentComponent Componente in cui centrare il dialog
     * @param message Messaggio da visualizzare
     */
    public static void showInformationMessage(Component parentComponent, String message) {
        showMessageDialog(parentComponent, message, "Messaggio", JOptionPane.INFORMATION_MESSAGE, null);
    }
}
