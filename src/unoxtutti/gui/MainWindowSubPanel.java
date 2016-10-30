/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
