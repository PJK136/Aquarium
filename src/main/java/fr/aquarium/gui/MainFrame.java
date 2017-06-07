package fr.aquarium.gui;

import fr.aquarium.ArduinoUsbChannel;
import fr.aquarium.Database;
import fr.aquarium.Extractor;
import fr.aquarium.Measure;
import fr.aquarium.MeasureEvent;
import fr.aquarium.MeasureListener;
import fr.aquarium.Monitor;
import fr.aquarium.PHCalibrationEvent;
import fr.aquarium.Receiver;
import fr.aquarium.Recorder;
import fr.aquarium.Sensor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.List;
import javax.mail.MessagingException;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrame extends javax.swing.JFrame {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public final static String SETTINGS_FILENAME = "settings.json";
            
    private Settings settings;
    private Database database;
    private Receiver receiver;
    private Recorder recorder;
    private Monitor monitor;
    private Extractor extractor;
    
    private Thread receiverThread;
    private boolean stopTriggered;
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        refreshArduinoPorts();
        loadSettings();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                super.windowClosing(we);
                if (receiverThread != null && receiverThread.isAlive()) {
                    receiver.stop();
                    try {
                        receiverThread.join();
                    } catch (InterruptedException ex) {
                        logger.error(null, ex);
                    }
                }
            }
        });
    }
    
    private void loadSettings() {
        try {
            settings = Settings.load(SETTINGS_FILENAME);
        } catch (Exception ex) {
            logger.error("Impossible de charger le fichier de configuration.", ex);
            settings = new Settings();
        }
        
        mysqlServer.setText(settings.mysqlServer);
        mysqlPort.setValue(settings.mysqlPort);
        mysqlDb.setText(settings.mysqlDb);
        mysqlUsername.setText(settings.mysqlUsername);
        mysqlPassword.setText(settings.mysqlPassword);
    
        arduinoPorts.setSelectedItem(settings.arduinoPort);

        emailUsername.setText(settings.emailUsername);
        emailPassword.setText(settings.emailPassword);
        
        DefaultListModel dlm = new DefaultListModel();
        for (String recipient : settings.recipients)
            dlm.addElement(recipient);
        recipients.setModel(dlm);
        
        fishNames.setSelectedItem(settings.fishName);

        lastMeasuresCount.setValue(settings.lastMeasuresCount);
        interval.setValue(settings.interval);
        json.setSelected(settings.json);
        csv.setSelected(settings.csv);
        
        ftpServer.setText(settings.ftpServer);
        ftpUsername.setText(settings.ftpUsername);
        ftpPassword.setText(settings.ftpPassword);
        ftpJSON.setSelected(settings.ftpJSON);
        ftpCSV.setSelected(settings.ftpCSV);
    }
    
    private void updateSettings() {
        settings.mysqlServer = mysqlServer.getText();
        settings.mysqlPort = (int) mysqlPort.getValue();
        settings.mysqlDb = mysqlDb.getText();
        settings.mysqlUsername = mysqlUsername.getText();
        settings.mysqlPassword = new String(mysqlPassword.getPassword());
    
        if (arduinoPorts.getSelectedItem() != null) {
            settings.arduinoPort = (String) arduinoPorts.getSelectedItem();
        }

        settings.emailUsername = emailUsername.getText();
        settings.emailPassword = new String(emailPassword.getPassword());

        ListModel listModel = recipients.getModel();
        settings.recipients.clear();
        for (int i = 0; i < listModel.getSize(); i++) {
            settings.recipients.add((String) listModel.getElementAt(i));
        }

        if (fishNames.getSelectedItem() != null) {
            settings.fishName = (String) fishNames.getSelectedItem();
        }
        
        settings.lastMeasuresCount = (int) lastMeasuresCount.getValue();
        settings.interval = (int) interval.getValue();
        settings.json = json.isSelected();
        settings.csv = csv.isSelected();
        
        settings.ftpServer = ftpServer.getText();
        settings.ftpUsername = ftpUsername.getText();
        settings.ftpPassword = new String(ftpPassword.getPassword());
        settings.ftpJSON = ftpJSON.isSelected();
        settings.ftpCSV = ftpCSV.isSelected();
        
        try {
            settings.save(SETTINGS_FILENAME);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            logger.error("Impossible de sauvegarder les paramètres dans " + SETTINGS_FILENAME, ex);
            JOptionPane.showMessageDialog(this,
                    "Impossible de sauvegarder les paramètres dans " + SETTINGS_FILENAME + "!",
                    "Erreur de sauvegarde",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshArduinoPorts() {
        String previouslySelected = (String) arduinoPorts.getSelectedItem();
        arduinoPorts.removeAllItems();
        for (String port : ArduinoUsbChannel.getComPorts()) {
            arduinoPorts.addItem(port);
        }
        arduinoPorts.setSelectedItem(previouslySelected);
    }
    
    private String recipientsToString() {
        updateSettings();
        StringBuilder builder = new StringBuilder();
        for (String recipient : settings.recipients) {
            builder.append(recipient);
            builder.append(",");
        }
        return builder.toString();
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

        jTextField2 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jLabel10 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        mysqlUsername = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        mysqlPassword = new javax.swing.JPasswordField();
        mysqlConnect = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        arduinoPorts = new javax.swing.JComboBox<>();
        arduinoRefreshPorts = new javax.swing.JButton();
        arduinoConnect = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        fishNames = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        emailPassword = new javax.swing.JPasswordField();
        jLabel13 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        jSeparator1 = new javax.swing.JSeparator();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 0));
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 0));
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        lastMeasuresCount = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        interval = new javax.swing.JSpinner();
        json = new javax.swing.JCheckBox();
        csv = new javax.swing.JCheckBox();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        supervisionStart = new javax.swing.JButton();
        emailUsername = new javax.swing.JTextField();
        newRecipient = new javax.swing.JTextField();
        recipientAdd = new javax.swing.JButton();
        recipientsRemove = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        mysqlPort = new javax.swing.JSpinner();
        mysqlDb = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        mysqlServer = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        emailTest = new javax.swing.JButton();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        jScrollPane3 = new javax.swing.JScrollPane();
        statusArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        recipients = new javax.swing.JList<>();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 32767));
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        ftpJSON = new javax.swing.JCheckBox();
        ftpCSV = new javax.swing.JCheckBox();
        ftpServer = new javax.swing.JTextField();
        ftpUsername = new javax.swing.JTextField();
        ftpPassword = new javax.swing.JPasswordField();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        ftpTest = new javax.swing.JButton();

        jTextField2.setText("jTextField1");

        jLabel9.setText("Identifiant : ");

        jPasswordField2.setText("jPasswordField1");

        jLabel10.setText("Mot de passe : ");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Superviseur d'Aquarium");
        setPreferredSize(new java.awt.Dimension(800, 600));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 48)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Superviseur d'Aquarium");
        getContentPane().add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jScrollPane2.setBorder(null);

        jPanel1.setName(""); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel2.setText("Identifiant : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel2, gridBagConstraints);

        mysqlUsername.setMinimumSize(new java.awt.Dimension(4, 27));
        mysqlUsername.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(mysqlUsername, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel3.setText("Mot de passe : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel3, gridBagConstraints);

        mysqlPassword.setMinimumSize(new java.awt.Dimension(4, 27));
        mysqlPassword.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(mysqlPassword, gridBagConstraints);

        mysqlConnect.setText("Se connecter");
        mysqlConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mysqlConnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(mysqlConnect, gridBagConstraints);

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel4.setText("MySQL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        jPanel1.add(filler2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(filler3, gridBagConstraints);

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel5.setText("Arduino");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel6.setText("Port :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel6, gridBagConstraints);

        arduinoPorts.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(arduinoPorts, gridBagConstraints);

        arduinoRefreshPorts.setText("Rafraîchir");
        arduinoRefreshPorts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arduinoRefreshPortsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(arduinoRefreshPorts, gridBagConstraints);

        arduinoConnect.setText("Se connecter");
        arduinoConnect.setEnabled(false);
        arduinoConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arduinoConnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(arduinoConnect, gridBagConstraints);

        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel7.setText("Population");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        jPanel1.add(filler5, gridBagConstraints);

        fishNames.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        fishNames.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(fishNames, gridBagConstraints);

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel8.setText("Email");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel8, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel11.setText("Identifiant : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel11, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel12.setText("Mot de passe : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel12, gridBagConstraints);

        emailPassword.setMinimumSize(new java.awt.Dimension(4, 27));
        emailPassword.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(emailPassword, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel13.setText("Destinataire :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel13, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 6;
        jPanel1.add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 6;
        jPanel1.add(filler6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        jPanel1.add(filler4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 6;
        jPanel1.add(filler7, gridBagConstraints);

        jLabel14.setFont(jLabel14.getFont().deriveFont(jLabel14.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel14.setText("JSON/CSV");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel14, gridBagConstraints);

        jLabel15.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel15.setText("Nombre :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel15, gridBagConstraints);

        lastMeasuresCount.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lastMeasuresCount.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1000));
        lastMeasuresCount.setMinimumSize(new java.awt.Dimension(31, 27));
        lastMeasuresCount.setPreferredSize(new java.awt.Dimension(31, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(lastMeasuresCount, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel16.setText("Intervalle (s) :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel16, gridBagConstraints);

        interval.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        interval.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        interval.setMinimumSize(new java.awt.Dimension(31, 27));
        interval.setPreferredSize(new java.awt.Dimension(31, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(interval, gridBagConstraints);

        json.setText("JSON");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(json, gridBagConstraints);

        csv.setText("CSV");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(csv, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 6;
        jPanel1.add(filler8, gridBagConstraints);

        supervisionStart.setText("Démarrer la supervision");
        supervisionStart.setEnabled(false);
        supervisionStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supervisionStartActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(supervisionStart, gridBagConstraints);

        emailUsername.setToolTipText("");
        emailUsername.setMinimumSize(new java.awt.Dimension(4, 27));
        emailUsername.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(emailUsername, gridBagConstraints);

        newRecipient.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        newRecipient.setMinimumSize(new java.awt.Dimension(4, 27));
        newRecipient.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(newRecipient, gridBagConstraints);

        recipientAdd.setText("Ajouter");
        recipientAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recipientAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(recipientAdd, gridBagConstraints);

        recipientsRemove.setText("Enlever");
        recipientsRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recipientsRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        jPanel1.add(recipientsRemove, gridBagConstraints);

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel17.setText("Port :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel17, gridBagConstraints);

        jLabel18.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel18.setText("BDD :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel18, gridBagConstraints);

        mysqlPort.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        mysqlPort.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        mysqlPort.setMinimumSize(new java.awt.Dimension(86, 27));
        mysqlPort.setPreferredSize(new java.awt.Dimension(31, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(mysqlPort, gridBagConstraints);

        mysqlDb.setMinimumSize(new java.awt.Dimension(4, 27));
        mysqlDb.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(mysqlDb, gridBagConstraints);

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel19.setText("Serveur :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel19, gridBagConstraints);

        mysqlServer.setMinimumSize(new java.awt.Dimension(4, 27));
        mysqlServer.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(mysqlServer, gridBagConstraints);

        jLabel20.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel20.setText("Poisson :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel20, gridBagConstraints);

        emailTest.setText("Tester");
        emailTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(emailTest, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(filler11, gridBagConstraints);

        statusArea.setEditable(false);
        statusArea.setColumns(20);
        statusArea.setLineWrap(true);
        statusArea.setRows(2);
        jScrollPane3.setViewportView(statusArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jScrollPane3, gridBagConstraints);

        recipients.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        recipients.setVisibleRowCount(3);
        jScrollPane1.setViewportView(recipients);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel1.add(filler12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(filler13, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 29;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(filler14, gridBagConstraints);

        jLabel21.setText("FTP");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel21, gridBagConstraints);

        jLabel22.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel22.setText("Serveur :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel22, gridBagConstraints);

        jLabel23.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel23.setText("Identifiant :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel23, gridBagConstraints);

        jLabel24.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel24.setText("Mot de passe :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel24, gridBagConstraints);

        ftpJSON.setText("JSON");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(ftpJSON, gridBagConstraints);

        ftpCSV.setText("CSV");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(ftpCSV, gridBagConstraints);

        ftpServer.setToolTipText("");
        ftpServer.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(ftpServer, gridBagConstraints);

        ftpUsername.setToolTipText("");
        ftpUsername.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(ftpUsername, gridBagConstraints);

        ftpPassword.setPreferredSize(new java.awt.Dimension(4, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(ftpPassword, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(filler9, gridBagConstraints);

        ftpTest.setText("Tester");
        ftpTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ftpTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(ftpTest, gridBagConstraints);

        jScrollPane2.setViewportView(jPanel1);

        getContentPane().add(jScrollPane2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mysqlConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mysqlConnectActionPerformed
        updateSettings();
        
        mysqlConnect.setText("...");
        try {
            database = new Database(settings.mysqlServer,
                    settings.mysqlPort,
                    settings.mysqlDb,
                    settings.mysqlUsername,
                    settings.mysqlPassword);
            
            mysqlConnect.setText("Connecté");
            this.arduinoConnect.setEnabled(true);
            
            List<String> fishNames = database.queryFishNames();
            if (fishNames != null) {
                this.fishNames.removeAllItems();
                for (String fishName : fishNames)
                    this.fishNames.addItem(fishName);
                this.fishNames.setEnabled(true);
            } else {
                this.fishNames.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                    "Impossible de charger la liste des poissons !",
                    "Erreur de chargement",
                    JOptionPane.ERROR_MESSAGE);
            }
            
            this.fishNames.setSelectedItem(settings.fishName);
        } catch (SQLException ex) {
            logger.error("Impossible de se connecter au serveur MySQL", ex);
            JOptionPane.showMessageDialog(this,
                    "Impossible de se connecter au serveur MySQL spécifié !",
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            database = null;
            mysqlConnect.setText("Se connecter");
            arduinoConnect.setText("Se connecter");
            this.fishNames.setEnabled(false);
            this.arduinoConnect.setEnabled(false);
            supervisionStart.setEnabled(false);
            receiver = null;
        }
    }//GEN-LAST:event_mysqlConnectActionPerformed

    private void arduinoRefreshPortsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arduinoRefreshPortsActionPerformed
        refreshArduinoPorts();
    }//GEN-LAST:event_arduinoRefreshPortsActionPerformed

    private void arduinoConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arduinoConnectActionPerformed
        updateSettings();
        
        if (settings.arduinoPort == null || settings.arduinoPort.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un port.",
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
            
        try {
            arduinoConnect.setText("...");
            receiver = new Receiver(database, settings.arduinoPort);
            arduinoConnect.setText("Connecté");
            supervisionStart.setEnabled(true);
        } catch (IOException ex) {
            logger.error("Impossible de se connecter à l'Arduino", ex);
            JOptionPane.showMessageDialog(this,
                    "Impossible de se connecter au port spécifié !",
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            arduinoConnect.setText("Se connecter");
            supervisionStart.setEnabled(false);
        }
    }//GEN-LAST:event_arduinoConnectActionPerformed

    private void emailTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailTestActionPerformed
        try {
            Monitor.sendEmail(settings.emailUsername,
                    settings.emailPassword,
                    recipientsToString(),
                    "[Aquarium] Email de test",
                    "Félicitations !\nVous avez bien configuré vos alertes par email.");
            JOptionPane.showMessageDialog(this,
                    "L'email de test a été envoyé à tous les destinataires !",
                    "Envoi de l'email",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (MessagingException ex) {
            logger.error("Erreur de l'envoi de l'email de test", ex);
            JOptionPane.showMessageDialog(this,
                    "Impossible d'envoyer l'email de test !",
                    "Erreur durant l'envoi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_emailTestActionPerformed

    private void recipientAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recipientAddActionPerformed
        if (newRecipient.getText() == null || newRecipient.getText().isEmpty())
            return;
        
        DefaultListModel<String> listModel = (DefaultListModel<String>) recipients.getModel();
        listModel.addElement(newRecipient.getText());
        newRecipient.setText("");
        updateSettings();
    }//GEN-LAST:event_recipientAddActionPerformed

    private void recipientsRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recipientsRemoveActionPerformed
        DefaultListModel<String> listModel = (DefaultListModel<String>) recipients.getModel();
        while (recipients.getSelectedIndex() >= 0) {
            listModel.removeElementAt(recipients.getSelectedIndex());
        }
        updateSettings();
    }//GEN-LAST:event_recipientsRemoveActionPerformed

    private void supervisionStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supervisionStartActionPerformed
        if (receiverThread == null || !receiverThread.isAlive()) {
            if (extractor != null)
                extractor.cancel();
            
            updateSettings();
            int fishId = database.queryFishId(settings.fishName);
            if (fishId < 0) {
                JOptionPane.showMessageDialog(this,
                        "Poisson non trouvé dans la base de données !",
                        "Erreur lors du lancement de la supervision",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            recorder = new Recorder(database);
            monitor = new Monitor(database, fishId, settings.emailUsername, settings.emailPassword, recipientsToString());
            extractor = new Extractor(database, settings.ftpServer, settings.ftpUsername, settings.ftpPassword);

            receiver.removeAllMeasureListener();
            receiver.addMeasureListener(recorder);
            receiver.addMeasureListener(monitor);
            receiver.addMeasureListener(new MeasureListener() {
                @Override
                public void measureReceived(MeasureEvent event) {
                    StringBuilder builder = new StringBuilder();
                    for (Measure measure : event.getMeasures()){
                        Sensor sensor = database.querySensor(measure.getSensorId());
                        builder.append(sensor.getName());
                        builder.append(" : ");
                        builder.append(measure.getValue());
                        builder.append(sensor.getUnitSuffix());
                        builder.append(", ");
                    }
                    
                    final String status = builder.toString();    
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            statusArea.setText(status);
                        }
                    });
                }

                @Override
                public void pHCalibrationReceived(PHCalibrationEvent event) {
                    
                }
            });

            extractor.schedule(settings.interval*1000, settings.lastMeasuresCount,
                    settings.json, settings.csv,
                    settings.ftpJSON, settings.ftpCSV);

            stopTriggered = false;
            receiverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    receiver.run();
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            extractor.cancel();
                            mysqlConnect.setEnabled(true);
                            mysqlServer.setEnabled(true);
                            mysqlPort.setEnabled(true);
                            mysqlDb.setEnabled(true);
                            mysqlUsername.setEnabled(true);
                            mysqlPassword.setEnabled(true);
                            arduinoPorts.setEnabled(true);
                            arduinoRefreshPorts.setEnabled(true);
                            arduinoConnect.setEnabled(true);
                            fishNames.setEnabled(true);
                            emailUsername.setEnabled(true);
                            emailPassword.setEnabled(true);
                            emailTest.setEnabled(true);
                            newRecipient.setEnabled(true);
                            recipientAdd.setEnabled(true);
                            recipientsRemove.setEnabled(true);
                            lastMeasuresCount.setEnabled(true);
                            interval.setEnabled(true);
                            json.setEnabled(true);
                            csv.setEnabled(true);
                            ftpServer.setEnabled(true);
                            ftpUsername.setEnabled(true);
                            ftpPassword.setEnabled(true);
                            ftpTest.setEnabled(true);
                            ftpJSON.setEnabled(true);
                            ftpCSV.setEnabled(true);
            
                            supervisionStart.setText("Démarrer la supervision");
                            if (stopTriggered == false) {
                                JOptionPane.showMessageDialog(MainFrame.this,
                                        "La supervision s'est arrêtée de manière imprévue !",
                                        "Erreur lors de la supervision",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                }
            });
            receiverThread.start();

            supervisionStart.setText("Supervision en cours...");
            mysqlConnect.setEnabled(false);
            mysqlServer.setEnabled(false);
            mysqlPort.setEnabled(false);
            mysqlDb.setEnabled(false);
            mysqlUsername.setEnabled(false);
            mysqlPassword.setEnabled(false);
            arduinoPorts.setEnabled(false);
            arduinoRefreshPorts.setEnabled(false);
            arduinoConnect.setEnabled(false);
            fishNames.setEnabled(false);
            emailUsername.setEnabled(false);
            emailPassword.setEnabled(false);
            emailTest.setEnabled(false);
            newRecipient.setEnabled(false);
            recipientAdd.setEnabled(false);
            recipientsRemove.setEnabled(false);
            lastMeasuresCount.setEnabled(false);
            interval.setEnabled(false);
            json.setEnabled(false);
            csv.setEnabled(false);
            ftpServer.setEnabled(false);
            ftpUsername.setEnabled(false);
            ftpPassword.setEnabled(false);
            ftpTest.setEnabled(false);
            ftpJSON.setEnabled(false);
            ftpCSV.setEnabled(false);
        } else {
            stopTriggered = true;
            receiver.stop();
        }
    }//GEN-LAST:event_supervisionStartActionPerformed

    private void ftpTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ftpTestActionPerformed
        try {
            updateSettings();
            if (!settings.ftpJSON && !settings.ftpCSV) {
                JOptionPane.showMessageDialog(this,
                    "Aucun fichier n'a été spécifié pour l'envoi sur le serveur FTP",
                    "Aucun fichié spécifié",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (settings.ftpJSON)
                Extractor.sendFTP(settings.ftpServer, settings.ftpUsername, settings.ftpPassword, Extractor.JSON_FILENAME);
            if (settings.ftpCSV)
                Extractor.sendFTP(settings.ftpServer, settings.ftpUsername, settings.ftpPassword, Extractor.CSV_FILENAME);

            JOptionPane.showMessageDialog(this,
                    "Le ou les fichiers spécifiés ont bien été envoyés sur le serveur FTP !",
                    "Envoi sur le serveur FTP",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            logger.error("Erreur durant l'envoi sur le serveur FTP", ex);
            JOptionPane.showMessageDialog(this,
                    "Impossible d'envoyer le ou les fichiers spécifiés sur le serveur FTP !",
                    "Erreur durant l'envoi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_ftpTestActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton arduinoConnect;
    private javax.swing.JComboBox<String> arduinoPorts;
    private javax.swing.JButton arduinoRefreshPorts;
    private javax.swing.JCheckBox csv;
    private javax.swing.JPasswordField emailPassword;
    private javax.swing.JButton emailTest;
    private javax.swing.JTextField emailUsername;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JComboBox<String> fishNames;
    private javax.swing.JCheckBox ftpCSV;
    private javax.swing.JCheckBox ftpJSON;
    private javax.swing.JPasswordField ftpPassword;
    private javax.swing.JTextField ftpServer;
    private javax.swing.JButton ftpTest;
    private javax.swing.JTextField ftpUsername;
    private javax.swing.JSpinner interval;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JCheckBox json;
    private javax.swing.JSpinner lastMeasuresCount;
    private javax.swing.JButton mysqlConnect;
    private javax.swing.JTextField mysqlDb;
    private javax.swing.JPasswordField mysqlPassword;
    private javax.swing.JSpinner mysqlPort;
    private javax.swing.JTextField mysqlServer;
    private javax.swing.JTextField mysqlUsername;
    private javax.swing.JTextField newRecipient;
    private javax.swing.JButton recipientAdd;
    private javax.swing.JList<String> recipients;
    private javax.swing.JButton recipientsRemove;
    private javax.swing.JTextArea statusArea;
    private javax.swing.JButton supervisionStart;
    // End of variables declaration//GEN-END:variables
}
