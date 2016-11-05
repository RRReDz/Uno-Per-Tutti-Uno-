/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.gui;

import javax.swing.JPanel;

/**
 *
 * @author picardi
 */
public class MainWindowSubPanel extends JPanel {
	protected UnoXTuttiGUI mainWindow;
	/**
	 * @return the mainWindow
	 */
	public UnoXTuttiGUI getMainWindow() {
		return mainWindow;
	}

	/**
	 * @param mainWindow the mainWindow to set
	 */
	public void setMainWindow(UnoXTuttiGUI mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void initializeContent() {
	}
}
