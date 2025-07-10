/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.iradraconis.jlsync;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author max
 */
public class SwitchSyncFrame extends javax.swing.JFrame {

    
    
    static String userHome = System.getProperty("user.home");
    static String directoryPath = userHome + "/.jL_Sync_Files_data";;
    static String filePath = directoryPath + "/jL_Sync_Files_Settings.json";
    static Path dirPath = Paths.get(directoryPath);
    static Path settingsPath = Paths.get(filePath);
    private String caseId;
   
    private final String server;
    private final String port;
    private final String user;
    private final String password;
    
    private CaseSearchPanel caseSearchPanel;

    /**
     * Creates new form SwitchSync
     */
    public SwitchSyncFrame(String server, String port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;

        initComponents();
        initializeSearchPanel();
        setSize(450, 450);
        setMinimumSize(new Dimension(420, 350));
        setLocationRelativeTo(null);
    }

    public static String[] findCaseIdByFileNumber(String fileNumber) throws FileNotFoundException {
        String filePath = directoryPath + "/" + "jL_Sync_Files_Cases.json";
        Gson gson = new Gson();

        // JSON-Datei lesen
        JsonArray jsonArray = JsonParser.parseReader(new FileReader(filePath)).getAsJsonArray();

        // Über alle Elemente iterieren und nach fileNumber suchen
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.get("fileNumber").getAsString().equals(fileNumber)) {
                String name = obj.get("name").getAsString();
                String id = obj.get("id").getAsString();
                return new String[]{name, id};
            }
        }
        return null;
    }

    public void switchSync(String caseId, String caseName, String server, String port, String user, String password, String onOff) throws FileNotFoundException {
        boolean toggle = false;

        String url = server + ":" + port + "/j-lawyer-io/rest/v5/cases/syncsettings";

        String auth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
        HttpClient client = HttpClient.newHttpClient();

        if (onOff.equals("on")) {
            toggle = true;
        } else if (onOff.equals("off")) {
            toggle = false;
        }

        String jsonBody = "{\"caseId\":\"" + caseId + "\",\"principalId\":\"" + user + "\",\"sync\":" + toggle + "}";
        System.out.println(jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(10))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();
            int statusCode = response.statusCode();
            System.out.println("Status Code: " + statusCode);
            System.out.println(jsonResponse);
            if (toggle) {
                jLabelSyncStatusL1.setText(caseName);
                jLabelSyncStatusL2.setText("wird synchronisiert");
            }
            if (!toggle) {
                jLabelSyncStatusL1.setText(caseName);
                jLabelSyncStatusL2.setText("wird nicht synchronisiert");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Verbindungsfehler");
            e.printStackTrace();
        }
    }
    
    private void initializeSearchPanel() {
        caseSearchPanel = new CaseSearchPanel();
        caseSearchPanel.setSearchSelectionListener(new CaseSearchPanel.SearchSelectionListener() {
            @Override
            public void onCaseSelected(FuzzySearchUtil.CaseResult selectedCase) {
                caseId = selectedCase.getId();
                updateSyncStatus(selectedCase.getName());
            }
        });
        
        jTextFieldFileNumber.setVisible(false);
        
        javax.swing.GroupLayout layout = (javax.swing.GroupLayout) getContentPane().getLayout();
        layout.replace(jTextFieldFileNumber, caseSearchPanel);
        
        revalidate();
        repaint();
    }
    
    private void updateSyncStatus(String caseName) {
        jLabelSyncStatusL1.setText(caseName);
        jLabelSyncStatusL2.setText("ausgewählt");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextFieldFileNumber = new javax.swing.JTextField();
        jButtonSyncOff = new javax.swing.JButton();
        jButtonSyncOn = new javax.swing.JButton();
        jLabelSyncStatusL2 = new javax.swing.JLabel();
        jLabelSyncStatusL1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Synchronisation der Akte:");

        jTextFieldFileNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldFileNumber.setText("Aktenzeichen eingeben...");
        jTextFieldFileNumber.setToolTipText("Aktenzeichen eingeben");

        jButtonSyncOff.setText("Sync OFF");
        jButtonSyncOff.setFont(new java.awt.Font("Cantarell", 1, 12));
        jButtonSyncOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSyncOffActionPerformed(evt);
            }
        });

        jButtonSyncOn.setText("Sync ON");
        jButtonSyncOn.setFont(new java.awt.Font("Cantarell", 1, 12));
        jButtonSyncOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSyncOnActionPerformed(evt);
            }
        });

        jLabelSyncStatusL2.setText(" ");
        jLabelSyncStatusL2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSyncStatusL2.setFont(new java.awt.Font("Cantarell", 0, 12));

        jLabelSyncStatusL1.setText(" ");
        jLabelSyncStatusL1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSyncStatusL1.setFont(new java.awt.Font("Cantarell", 1, 13));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldFileNumber)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonSyncOn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSyncOff, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabelSyncStatusL2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelSyncStatusL1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addGap(35, 35, 35))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(25, 25, 25)
                .addComponent(jTextFieldFileNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSyncOff, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSyncOn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jLabelSyncStatusL1)
                .addGap(8, 8, 8)
                .addComponent(jLabelSyncStatusL2)
                .addGap(25, 25, Short.MAX_VALUE))
        );

    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSyncOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSyncOffActionPerformed
        try {
            String[] caseIdAndName;
            String searchText = caseSearchPanel.getSearchText();

            if (searchText.isEmpty()) {
                jLabelSyncStatusL1.setText("Fehler");
                jLabelSyncStatusL2.setText("Bitte Akte auswählen");
                return;
            }

            if (caseId == null) {
                caseIdAndName = findCaseIdByFileNumber(searchText);
                if (caseIdAndName == null) {
                    jLabelSyncStatusL1.setText("Fehler");
                    jLabelSyncStatusL2.setText("Akte nicht gefunden");
                    return;
                }
                caseId = caseIdAndName[1];
            } else {
                caseIdAndName = findCaseIdByFileNumber(searchText);
                if (caseIdAndName == null) {
                    jLabelSyncStatusL1.setText("Fehler");
                    jLabelSyncStatusL2.setText("Akte nicht gefunden");
                    return;
                }
            }
            
            System.out.println("caseId: " + caseId);
            System.out.println("caseName: " + caseIdAndName[0]);

            switchSync(caseId, caseIdAndName[0], server, port, user, password, "off");
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SwitchSyncFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonSyncOffActionPerformed

    private void jButtonSyncOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSyncOnActionPerformed
        try {
            String[] caseIdAndName;
            String searchText = caseSearchPanel.getSearchText();

            if (searchText.isEmpty()) {
                jLabelSyncStatusL1.setText("Fehler");
                jLabelSyncStatusL2.setText("Bitte Akte auswählen");
                return;
            }

            if (caseId == null) {
                caseIdAndName = findCaseIdByFileNumber(searchText);
                if (caseIdAndName == null) {
                    jLabelSyncStatusL1.setText("Fehler");
                    jLabelSyncStatusL2.setText("Akte nicht gefunden");
                    return;
                }
                caseId = caseIdAndName[1];
            } else {
                caseIdAndName = findCaseIdByFileNumber(searchText);
                if (caseIdAndName == null) {
                    jLabelSyncStatusL1.setText("Fehler");
                    jLabelSyncStatusL2.setText("Akte nicht gefunden");
                    return;
                }
            }
            
            System.out.println("caseId: " + caseId);
            System.out.println("caseName: " + caseIdAndName[0]);

            switchSync(caseId, caseIdAndName[0], server, port, user, password, "on");
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SwitchSyncFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonSyncOnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {        
        // Argumente lesen
        String server = args[0];
        String port = args[1];
        String user = args[2];
        String password = args[3];
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SwitchSyncFrame(server, port, user, password).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonSyncOff;
    private javax.swing.JButton jButtonSyncOn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelSyncStatusL1;
    private javax.swing.JLabel jLabelSyncStatusL2;
    private javax.swing.JTextField jTextFieldFileNumber;
    // End of variables declaration//GEN-END:variables
}
