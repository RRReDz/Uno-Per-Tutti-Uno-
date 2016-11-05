/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti;

import unoxtutti.gui.AutenticarsiGUI;
import unoxtutti.gui.UnoXTuttiGUI;

/**
 * Classe che rappresenta l'applicazione nel suo complesso. Gestisce il passaggio
 * di consegne fra i controller GRASP coinvolti nell'UC principale (inizialmente
 * "Autenticarsi", successivamente "GiocareAUnoXTutti").
 * Lancia la GUI dell'applicazione stabilendo la sua relazione coi controller.
 * Definisce variabili d'ambiente dell'applicazione (fra cui le istanze dei controller
 * GRASP)
 *
 * @author picardi
 */
public class UnoXTutti {

    /**
     * il controller GRASP per "Autenticarsi"
     */
    public static AutenticarsiController theAutController;

    /**
     * il controller GRASP per "Giocare a UnoXTutti"
     */
    public static GiocareAUnoXTuttiController theUxtController;

    /**
     * L'indirizzo del Web Server simulato, modificare se gira su un'altra
     * macchina.
     */
    public static String WEB_ADDRESS = "localhost";

    /**
     * Porta del Web Server simulato, modificare se gira su un'altra porta.
     */
    public static int WEB_PORT = 9000;

    /**
     * La GUI principale dell'applicazione.
     */

    public static UnoXTuttiGUI mainWindow;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // PRIMO PASSO: inizializzare il controller di Autenticarsi e mostrare la GUI

        theAutController = AutenticarsiController.getInstance();
        boolean ok = theAutController.initialize();
        if (!ok) {
            System.out.println("Problemi di inizializzazione dell'autenticazione. Termino.");
            return;
        }
        AutenticarsiGUI autDialog = new AutenticarsiGUI(null, true);
        autDialog.setVisible(true);
        autDialog.dispose();

        // Capiamo che Autenticarsi ha avuto successo dal fatto che il controller di
        // Autenticarsi ha ottenuto un Player

        if (theAutController.getPlayer() == null) // usciamo
            return;

        // Autenticazione riuscita. Ora attiviamo il GiocareAUnoXTuttiController e
        // tiriamo su la GUI principale dell'applicazione

        theUxtController = GiocareAUnoXTuttiController.getInstance(theAutController.getPlayer());

		/*To add for DEBUG:
        theUxtController = GiocareAUnoXTuttiController.getInstance(Player.createPlayer(new RegisteredPlayer(0,"beatrice","","")));
		*/
                
		/* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                mainWindow = new UnoXTuttiGUI();
                mainWindow.setVisible(true);
            }
        });

        // Attenzione: la MainWindow è settata su DISPOSE ON CLOSE invece che EXIT ON CLOSE
        // perchè se la finestra viene chiusa invece che uscire bruscamente è meglio chiudere
        // i thread in modo graceful.
        // Quindi bisogna poi mettere un listener sulla finestra (presumibilmente lo stesso
        // GiocareAUnoXTuttiController) che gestisca la chiusura di tutto quanto.
        // TODO
    }

}
