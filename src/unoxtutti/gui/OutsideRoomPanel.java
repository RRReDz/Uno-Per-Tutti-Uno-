/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.gui;

import javax.swing.JOptionPane;
import unoxtutti.UnoXTutti;
import unoxtutti.connection.ClientConnectionException;
import unoxtutti.domain.ServerRoom;
import unoxtutti.utils.DebugHelper;
import unoxtutti.utils.GUIUtils;

/**
 *
 * @author picardi
 */
public class OutsideRoomPanel extends MainWindowSubPanel {

    /**
     * Creates new form OutsideRoomPanel
     */
    private ServerRoom selectedRoom;

    public OutsideRoomPanel() {
        initComponents();
    }

    @Override
    public void initializeContent() {
        this.stanzeList.setModel(UnoXTutti.theUxtController.getServerRoomNames());
        this.stanzeList.setSelectedIndex(-1);
        this.chiudiStanzaButton.setEnabled(false);
        this.infoButton.setEnabled(false);
        this.ipStanzaField.setText("localhost");
        this.portaStanzaField.setText("");
        this.nomeStanzaField.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        nomeStanzaField = new javax.swing.JTextField();
        javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        ipStanzaField = new javax.swing.JTextField();
        javax.swing.Box.Filler filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        portaStanzaField = new javax.swing.JTextField();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        entraButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        stanzeList = new javax.swing.JList<>();
        javax.swing.JPanel jPanel6 = new javax.swing.JPanel();
        apriStanzaButton = new javax.swing.JButton();
        infoButton = new javax.swing.JButton();
        chiudiStanzaButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jSplitPane1.setDividerLocation(300);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jLabel1.setText("Nome Stanza:");
        jPanel1.add(jLabel1);

        nomeStanzaField.setColumns(30);
        nomeStanzaField.setAlignmentX(0.0F);
        jPanel1.add(nomeStanzaField);
        jPanel1.add(filler1);

        jLabel2.setText("Indirizzo Stanza:");
        jPanel1.add(jLabel2);

        ipStanzaField.setColumns(30);
        ipStanzaField.setText("localhost");
        ipStanzaField.setAlignmentX(0.0F);
        jPanel1.add(ipStanzaField);
        jPanel1.add(filler2);

        jLabel3.setText("Porta:");
        jPanel1.add(jLabel3);

        portaStanzaField.setColumns(5);
        portaStanzaField.setAlignmentX(0.0F);
        jPanel1.add(portaStanzaField);

        jPanel2.add(jPanel1);

        jPanel4.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        entraButton.setText("Entra");
        entraButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entraButtonActionPerformed(evt);
            }
        });
        jPanel3.add(entraButton);

        jPanel4.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jPanel4);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jLabel4.setText("Le tue stanze:");
        jPanel5.add(jLabel4, java.awt.BorderLayout.PAGE_START);

        stanzeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        stanzeList.setToolTipText("");
        stanzeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                stanzeListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(stanzeList);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 5));

        apriStanzaButton.setText("Apri");
        apriStanzaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriStanzaButtonActionPerformed(evt);
            }
        });
        jPanel6.add(apriStanzaButton);

        infoButton.setText("Info");
        infoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoButtonActionPerformed(evt);
            }
        });
        jPanel6.add(infoButton);

        chiudiStanzaButton.setText("Chiudi");
        chiudiStanzaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chiudiStanzaButtonActionPerformed(evt);
            }
        });
        jPanel6.add(chiudiStanzaButton);

        jPanel5.add(jPanel6, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setLeftComponent(jPanel5);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void stanzeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_stanzeListValueChanged
        if (!evt.getValueIsAdjusting()) {
            int sel = stanzeList.getSelectedIndex();
            this.chiudiStanzaButton.setEnabled(sel >= 0);
            this.infoButton.setEnabled(sel >= 0);
            if (sel >= 0) {
                String rname = (String) stanzeList.getSelectedValue();
                selectedRoom = UnoXTutti.theUxtController.getRoom(rname);
                this.ipStanzaField.setText("localhost");
                this.portaStanzaField.setText(selectedRoom.getPort() + "");
                this.nomeStanzaField.setText(rname);
            }
        }
    }//GEN-LAST:event_stanzeListValueChanged

    private void apriStanzaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriStanzaButtonActionPerformed
        NuovaStanzaDialog dia = new NuovaStanzaDialog(mainWindow, true);
        GUIUtils.centerDialogInsideWindow(dia, mainWindow);
        dia.setVisible(true);
        if (dia.getResult() == JOptionPane.OK_OPTION) {
            DebugHelper.log("OK: Avvio interfaccia gestione interna stanza.");
            UnoXTutti.theUxtController.apriStanza(dia.getRoomName(), dia.getRoomPort());
        }
    }//GEN-LAST:event_apriStanzaButtonActionPerformed

    private void chiudiStanzaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chiudiStanzaButtonActionPerformed
        if (selectedRoom != null) {
            UnoXTutti.theUxtController.chiudiStanza(selectedRoom);
        }
    }//GEN-LAST:event_chiudiStanzaButtonActionPerformed

    private void infoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoButtonActionPerformed
        if (selectedRoom != null) {
            GUIUtils.showInformationMessage(mainWindow, selectedRoom.getInfo());
        }
        DebugHelper.log("ERR: Nessuna stanza selezionata.");
    }//GEN-LAST:event_infoButtonActionPerformed

    private void entraButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entraButtonActionPerformed
        String roomName = this.nomeStanzaField.getText().trim();
        String roomAddr = this.ipStanzaField.getText().trim();
        int roomPort = 0;
        try {
            roomPort = Integer.parseInt(this.portaStanzaField.getText().trim());
        } catch (NumberFormatException ex) {
            roomPort = 0;
        }
        if (roomPort < 1024 || roomPort > 65535) {
            GUIUtils.showInformationMessage(this, "La porta deve essere un numero\ncompreso fra 1024 e 65535");
            DebugHelper.log("ERR: La porta deve essere un numero compreso fra 1024 e 65535.");
        } else {
            this.mainWindow.setWaiting(true);
            DebugHelper.log("Richiesta di accesso in stanza (" + roomName + ", " + roomAddr + ", " + roomPort);
            try {
                UnoXTutti.theUxtController.entraInStanza(roomName, roomAddr, roomPort);
            } catch (ClientConnectionException exc) {
                DebugHelper.log("ERR: L'indirizzo " + roomAddr + " NON è corretto.");
            }
            this.mainWindow.setWaiting(false);
            if (UnoXTutti.theUxtController.inStanza()) {
                DebugHelper.log("OK: Avvio interfaccia gestione partita.");
                this.mainWindow.setGuiState(UnoXTuttiGUI.GUIState.INSIDE_ROOM);
            } else {
                GUIUtils.showErrorMessage(mainWindow, "Errore durante l'accesso alla stanza, dati errati.");
            }
        }
    }//GEN-LAST:event_entraButtonActionPerformed

    /**
     * @return the mainWindow
     */
    @Override
    public UnoXTuttiGUI getMainWindow() {
        return mainWindow;
    }

    /**
     * @param mainWindow the mainWindow to set
     */
    @Override
    public void setMainWindow(UnoXTuttiGUI mainWindow) {
        this.mainWindow = mainWindow;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton apriStanzaButton;
    private javax.swing.JButton chiudiStanzaButton;
    private javax.swing.JButton entraButton;
    private javax.swing.JButton infoButton;
    private javax.swing.JTextField ipStanzaField;
    private javax.swing.JTextField nomeStanzaField;
    private javax.swing.JTextField portaStanzaField;
    private javax.swing.JList<String> stanzeList;
    // End of variables declaration//GEN-END:variables
}
