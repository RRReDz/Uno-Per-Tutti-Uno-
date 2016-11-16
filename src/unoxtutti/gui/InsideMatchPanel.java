/* 
 * Progetto UnoXTutto per l'esame di Sviluppo Applicazione Software.
 * Rossi Riccardo, Giacobino Davide, Sguotti Leonardo
 */
package unoxtutti.gui;

import javax.swing.JOptionPane;
import unoxtutti.GiocarePartitaController;
import unoxtutti.UnoXTutti;
import unoxtutti.domain.Player;
import unoxtutti.domain.RemoteMatch;
import unoxtutti.utils.GUIUtils;

/**
 *
 * @author Riccardo Rossi
 */
public class InsideMatchPanel extends MainWindowSubPanel{

    private RemoteMatch currentMatch;
    
    /**
     * Creates new form InsideMatchPanel
     */
    public InsideMatchPanel() {
        initComponents();
    }
    
    @Override
    public void initializeContent() {
        currentMatch = GiocarePartitaController.getInstance().getCurrentMatch();
        this.playersList.setModel(currentMatch.getPlayersAsList());
        this.matchNameLabel.setText("Partita: " + currentMatch.getMatchName());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        matchNameLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        playersList = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        startMatchButton = new javax.swing.JButton();
        closeMatchButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        jPanel1.setLayout(new java.awt.BorderLayout());

        matchNameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        matchNameLabel.setText("Partita:");
        jPanel1.add(matchNameLabel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel2.setLayout(new java.awt.BorderLayout());

        playersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        playersList.setName("playersList"); // NOI18N
        jScrollPane1.setViewportView(playersList);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Giocatori:");
        jPanel2.add(jLabel1, java.awt.BorderLayout.PAGE_START);

        startMatchButton.setText("Avvia la partita");
        startMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startMatchButtonActionPerformed(evt);
            }
        });
        jPanel3.add(startMatchButton);

        closeMatchButton.setText("Chiudi la partita");
        closeMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMatchButtonActionPerformed(evt);
            }
        });
        jPanel3.add(closeMatchButton);

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void startMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startMatchButtonActionPerformed
        if(currentMatch.getPlayersAsList().getSize() <= 1) {
            /* Errore, nessun giocatore all'interno della partita */
            GUIUtils.ShowErrorMessage(mainWindow, "Nella partita non ci sono abbastanza giocatori!", "Errore");
        } else {
            /* Tentativo di avvio della partita */
            this.mainWindow.setWaiting(true);
            GiocarePartitaController.getInstance().avviaPartita();
            this.mainWindow.setWaiting(false);
        }
    }//GEN-LAST:event_startMatchButtonActionPerformed

    private void closeMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMatchButtonActionPerformed
        /* Torna indietro alla stanza, ATTENZIONE: non viene ancora fatta la chiamata verso il server */
        //mainWindow.setGuiState(UnoXTuttiGUI.GUIState.INSIDE_ROOM);
    }//GEN-LAST:event_closeMatchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeMatchButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel matchNameLabel;
    private javax.swing.JList<Player> playersList;
    private javax.swing.JButton startMatchButton;
    // End of variables declaration//GEN-END:variables
}
