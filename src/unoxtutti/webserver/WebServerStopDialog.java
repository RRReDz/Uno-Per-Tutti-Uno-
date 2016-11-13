/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.webserver;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import unoxtutti.configuration.DebugConfig;
import unoxtutti.utils.TimeUtils;

/**
 *
 * @author picardi
 */
public class WebServerStopDialog extends javax.swing.JDialog {

    private final WebServer webServer;

    /**
     * Creates new form WebServerStopDialog
     * @param parent Frame di appartenenza
     * @param ws WebServer
     */
    public WebServerStopDialog(java.awt.Frame parent, WebServer ws) {
        super(parent, java.awt.Dialog.ModalityType.TOOLKIT_MODAL);
        initComponents();
        webServer = ws;
    }

    /**
     * Aggiunge un messaggio alla console.
     * @param message Messaggio
     */
    public void appendMessageToConsole(String message) {
        synchronized(consoleTxt) {
            String prefix = "[" + TimeUtils.getCurrentTimeStamp("HH:mm:ss") + "] ";
            consoleTxt.append(prefix);
            consoleTxt.append(message.endsWith("\n") ? message : message + "\n");
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        footerPanel = new javax.swing.JPanel();
        clearConsoleButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        minimizeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(DebugConfig.CONSOLE_TITLE + " - WebServer");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("../resources/icons/terminal.png")));
        setPreferredSize(new java.awt.Dimension(500, 200));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        consoleTxt.setEditable(false);
        consoleTxt.setColumns(20);
        consoleTxt.setRows(5);
        jScrollPane1.setViewportView(consoleTxt);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        footerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        footerPanel.setLayout(new java.awt.GridBagLayout());

        clearConsoleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/unoxtutti/resources/icons/trash.png"))); // NOI18N
        clearConsoleButton.setLabel("Pulisci console");
        clearConsoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearConsoleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        footerPanel.add(clearConsoleButton, gridBagConstraints);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/unoxtutti/resources/icons/stop.png"))); // NOI18N
        stopButton.setText("Stop Web Server");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        footerPanel.add(stopButton, gridBagConstraints);

        minimizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/unoxtutti/resources/icons/minimize.png"))); // NOI18N
        minimizeButton.setText("Minimizza");
        minimizeButton.setToolTipText("Nasconde la finestra nell'area di notifica del sistema.");
        minimizeButton.setEnabled(false);
        minimizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        footerPanel.add(minimizeButton, gridBagConstraints);

        getContentPane().add(footerPanel, java.awt.BorderLayout.PAGE_END);

        setBounds(790, 460, 580, 250);
    }// </editor-fold>//GEN-END:initComponents

    
    /**
     * Attende la terminazione del server
     */
    @SuppressWarnings("empty-statement")
    private void waitForServerToStop() {
        webServer.setStopSuggested(true);
        while (!webServer.isStopped());
    }
    
    /**
     * Richiamata quando viene cliccato il bottone per fermare il server
     * e chiudere l'applicazione.
     * @param evt Evento generato dal click del bottone
     */
    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        waitForServerToStop();
        setVisible(false);
        dispatchEvent(new WindowEvent(
                this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_stopButtonActionPerformed

    /**
     * Richiamata quando viene chiusa l'interfaccia grafica del Web Server
     * tramite il bottone di chiusura in alto a destra.
     * @param evt Evento generato dal click del bottone
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(!webServer.isStopped()) {
            waitForServerToStop();
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * Richiamata quando viene cliccato il bottone per pulire la console.
     * @param evt Evento generato dal click del bottone
     */
    private void clearConsoleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearConsoleButtonActionPerformed
        synchronized(consoleTxt) {
            consoleTxt.setText("");
        }
    }//GEN-LAST:event_clearConsoleButtonActionPerformed

    /**
     * Minimizza la finestra nell'area di notifica del sistema.
     * @param evt 
     */
    private void minimizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeButtonActionPerformed
        // TODO: Bugfix
        if(true) throw new UnsupportedOperationException("Da correggere...");
        
        if(SystemTray.isSupported()) {
            try {
                /* Recupero System Tray ed icona */
                SystemTray tray = SystemTray.getSystemTray();
                BufferedImage icon = ImageIO.read(WebServerStopDialog.class.getResource("../resources/icons/terminal-white.png"));
                
                /* Creazione del menù da aggiungere alla System Tray*/
                TrayIcon trayIcon;
                trayIcon = new TrayIcon(icon, "UnoXTutti - Web Server");
                trayIcon.setImageAutoSize(true);
                
                /* Creazione listener per ripristinare la finestra */
                trayIcon.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getButton() != 1 && e.getButton() != 3) return;
                        /* Ripristino finestra */
                        setVisible(true);
                        toFront();
                        requestFocusInWindow();
                        // TODO: Bugfix ripristino finestra
                        SystemTray.getSystemTray().remove(trayIcon);
                    }
                    @Override
                    public void mousePressed(MouseEvent e) { }
                    @Override
                    public void mouseReleased(MouseEvent e) { }
                    @Override
                    public void mouseEntered(MouseEvent e) { }
                    @Override
                    public void mouseExited(MouseEvent e) { }
                });
                
                /* Minimizzazione applicazione */
                tray.add(trayIcon);
                setVisible(false);
            } catch (AWTException | IOException ex) {
                Logger.getLogger(WebServerStopDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_minimizeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearConsoleButton;
    private final javax.swing.JTextArea consoleTxt = new javax.swing.JTextArea();
    private javax.swing.JPanel footerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton minimizeButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables
}
