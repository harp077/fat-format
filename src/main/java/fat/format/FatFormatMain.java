package fat.format;

import de.waldheinz.fs.BlockDevice;
import de.waldheinz.fs.fat.FatType;
import de.waldheinz.fs.fat.SuperFloppyFormatter;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.ItemEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.StringUtils;

public class FatFormatMain extends javax.swing.JFrame {

    public static Boolean running = false;
    public static int MAX_CONCURRENT_LOGINS = 11;
    public static int MAX_CONCURRENT_LOGINS_PER_IP = 11;
    public static FatFormatMain frame;
    public static int FW = 800;
    public static int FH = 400;
    public static List<String> lookAndFeelsDisplay = new ArrayList<>();
    public static List<String> lookAndFeelsRealNames = new ArrayList<>();
    public static Map<String, String> argsHM = new HashMap<String, String>();
    public static Thread Log_Thread;
    public static List<String> listStoresStrings = new ArrayList<>();
    public static String selectedStore;
    public static FileStore selectedUSB;
    public static BlockDevice usbBlockDevice;
    public static long sizeUsbBlockDevice;
    public static SuperFloppyFormatter SFF;
    public static String[] arrayFatTypes = {" FAT-12 ", " FAT-16 ", " FAT-32 "};
    public static String[] arrayClusterSize = {"512", "1024", "2048", "4096", "8192", "16384", "32768"};
    public static Map<String, FatType> fatMap = new HashMap<String, FatType>();
    public static Map<String, Long> fatMapMaxClusters = new HashMap<String, Long>();
    /*public static long maxClustersFat12 = Math.round(Math.pow(2,12));
    public static long maxClustersFat16 = Math.round(Math.pow(2,16));
    public static long maxClustersFat32 = Math.round(Math.pow(2,32));*/
    public static String currentLAF = "de.muntjak.tinylookandfeel.TinyLookAndFeel";
    //public static String currentLAF = "javax.swing.plaf.metal.MetalLookAndFeel";
    public static String zagolovok = " FAT format, v1.0.1, build  06-02-2021";

    public FatFormatMain() {
        //BlockDevice dev = new RamDisk(16700000);
        /*try {
            FatFileSystem fs = SuperFloppyFormatter.get(dev).setVolumeLabel("CF").setFatType(FatType.FAT12).format();
            //fs.
        } catch (IOException ex) {
            Logger.getLogger(FatFormatMain.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        initComponents();
        ImageIcon icone = new ImageIcon(getClass().getResource("/img/top-frame-triangle-16.png"));
        this.setIconImage(icone.getImage());
        this.setTitle(zagolovok);
        try {
            if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
                for (FileStore store : FileSystems.getDefault().getFileStores()) {
                    //if (store.toString().contains("/dev/") && store.getTotalSpace() > 0 && store.getTotalSpace() < 2147483647 && !store.toString().contains("tmp") && !store.toString().contains("shm")) {
                    if (store.toString().contains("/dev/") && store.getTotalSpace() > 0 && !store.toString().contains("tmp") && !store.toString().contains("shm")) {    
                        listStoresStrings.add(store.toString().trim());
                    }
                }
            } else {
                for (FileStore store : FileSystems.getDefault().getFileStores()) {
                    //if (store.getTotalSpace() > 0 && store.getTotalSpace() < 2147483647 && !store.toString().contains("tmp") && !store.toString().contains("shm")) {
                    if (store.getTotalSpace() > 0 && !store.toString().contains("tmp") && !store.toString().contains("shm")) {    
                        listStoresStrings.add(store.toString().trim());
                    }
                }                
            }
        } catch (IOException io) {
        }
        this.comboStoresStringsList.setModel(new DefaultComboBoxModel<>(listStoresStrings.stream().toArray(String[]::new)));
        this.comboStoresStringsList.setEditable(false);
        this.selectedStore = comboStoresStringsList.getSelectedItem().toString().trim();
        this.comboSelectFAT.setModel(new DefaultComboBoxModel<>(arrayFatTypes));
        this.comboClusterSize.setModel(new DefaultComboBoxModel<>(arrayClusterSize));
        this.taLog.append("Selected Device: " + selectedStore + "\n");
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            if (store.toString().equals(selectedStore)) {
                selectedUSB = store;
                if (selectedUSB.type().equals("ext3")||selectedUSB.type().equals("ext4")||selectedUSB.type().equals("swap")) {
                    listStoresStrings.remove(selectedStore);
                    comboStoresStringsList.setModel(new DefaultComboBoxModel<>(listStoresStrings.stream().toArray(String[]::new)));
                }
            }
        }
        this.selectedStore = comboStoresStringsList.getSelectedItem().toString().trim();
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            if (store.toString().equals(selectedStore)) {
                selectedUSB = store;
            }
        }        
        try {
            this.tfSize.setText("" + selectedUSB.getTotalSpace());
            this.sizeUsbBlockDevice=selectedUSB.getTotalSpace();
            taLog.append("Size Device: " + sizeUsbBlockDevice + "\n");            
        } catch (IOException ex) {
            Logger.getLogger(FatFormatMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*usbBlockDevice = new RamDisk(65535, 8192);
        try {
            SFF = SuperFloppyFormatter.get(usbBlockDevice);
        } catch (IOException ex) {
            Logger.getLogger(FatFormatMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        tfOldFatType.setText(SFF.getFatType().toString());*/
        this.tfName.setText(selectedUSB.name());
        this.tfType.setText(selectedUSB.type());
        this.taLog.setBackground(Color.DARK_GRAY);
        this.taLog.setForeground(Color.CYAN);
        fatMapsInit();
        this.btnClearLog.setVisible(false);
        System.out.println("maxClustersFat12 = "+fatMapMaxClusters.get("FAT-12")+"\n");
        System.out.println("maxClustersFat16 = "+fatMapMaxClusters.get("FAT-16")+"\n");
        System.out.println("maxClustersFat32 = "+fatMapMaxClusters.get("FAT-32")+"\n");
    }

    public static void MyInstLF(String lf) {
        //UIManager.installLookAndFeel(lf,lf);  
        lookAndFeelsDisplay.add(lf);
        lookAndFeelsRealNames.add(lf);
    }
    
    public static void fatMapsInit() {
        fatMap.put("FAT-12", FatType.FAT12);
        fatMap.put("FAT-16", FatType.FAT16);
        fatMap.put("FAT-32", FatType.FAT32);
        fatMapMaxClusters.put("FAT-12", Math.round(Math.pow(2,12)));
        fatMapMaxClusters.put("FAT-16", Math.round(Math.pow(2,16)));
        fatMapMaxClusters.put("FAT-32", Math.round(Math.pow(2,32)));        
    }    

    /*public void changeLF() {
        String changeLook = (String) JOptionPane.showInputDialog(frame, "Choose Look and Feel Here:", "Select Look and Feel", JOptionPane.QUESTION_MESSAGE, new ImageIcon(getClass().getResource("/img/color_swatch.png")), lookAndFeelsDisplay.toArray(), null);
        if (changeLook != null) {
            for (int a = 0; a < lookAndFeelsDisplay.size(); a++) {
                if (changeLook.equals(lookAndFeelsDisplay.get(a))) {
                    currentLAF = lookAndFeelsRealNames.get(a);
                    setLF(frame);
                    break;
                }
            }
        }
    }*/
    public void setLF(JFrame frame) {
        try {
            UIManager.setLookAndFeel(currentLAF);
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
            Logger.getLogger(this.getName()).log(Level.WARNING, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(frame);
        //frame.pack();
    }

    public static void InstallLF() {
        MyInstLF("javax.swing.plaf.metal.MetalLookAndFeel");
        MyInstLF("de.muntjak.tinylookandfeel.TinyLookAndFeel");
    }

    private void setBooleanBtnTf(Boolean sset) {
        tfName.setEditable(sset);
        tfType.setEditable(sset);
        tfSize.setEditable(sset);
        comboStoresStringsList.setEnabled(sset);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jLabel4 = new javax.swing.JLabel();
        comboStoresStringsList = new javax.swing.JComboBox<>();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        tfSize = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        tfType = new javax.swing.JTextField();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        jLabel7 = new javax.swing.JLabel();
        tfOldFatType = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taLog = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        jLabel5 = new javax.swing.JLabel();
        comboSelectFAT = new javax.swing.JComboBox<>();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jLabel8 = new javax.swing.JLabel();
        comboClusterSize = new javax.swing.JComboBox<>();
        jSeparator14 = new javax.swing.JToolBar.Separator();
        jLabel9 = new javax.swing.JLabel();
        tfMaxSizeForSelectedFatAndCluster = new javax.swing.JTextField();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        jLabel6 = new javax.swing.JLabel();
        tfVolumeLabel = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btnToggleRunStop = new javax.swing.JToggleButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        jToolBar3 = new javax.swing.JToolBar();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        btnAbout = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnQuit = new javax.swing.JButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        btnClearLog = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("pj-ftp-server");
        setLocation(new java.awt.Point(99, 99));
        setMinimumSize(new java.awt.Dimension(800, 500));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(800, 500));
        setSize(new java.awt.Dimension(800, 500));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Device"));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.add(jSeparator5);

        jLabel4.setText("Device List: ");
        jToolBar1.add(jLabel4);

        comboStoresStringsList.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "127.0.0.1" }));
        comboStoresStringsList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboStoresStringsListActionPerformed(evt);
            }
        });
        jToolBar1.add(comboStoresStringsList);
        jToolBar1.add(jSeparator12);

        jLabel1.setText("Total Size: ");
        jToolBar1.add(jLabel1);

        tfSize.setEditable(false);
        tfSize.setText("21");
        jToolBar1.add(tfSize);
        jToolBar1.add(jSeparator1);

        jLabel2.setText("Name: ");
        jToolBar1.add(jLabel2);

        tfName.setEditable(false);
        jToolBar1.add(tfName);
        jToolBar1.add(jSeparator2);

        jLabel3.setText("FS type: ");
        jToolBar1.add(jLabel3);

        tfType.setEditable(false);
        jToolBar1.add(tfType);
        jToolBar1.add(jSeparator9);

        jLabel7.setText("FAT type: ");
        jToolBar1.add(jLabel7);

        tfOldFatType.setEditable(false);
        jToolBar1.add(tfOldFatType);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Log-File content - /log/app.log"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        taLog.setColumns(20);
        taLog.setRows(5);
        jScrollPane2.setViewportView(taLog);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jToolBar2.setBorder(javax.swing.BorderFactory.createTitledBorder("Format Device"));
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.add(jSeparator13);

        jLabel5.setText("Select FAT type: ");
        jToolBar2.add(jLabel5);

        comboSelectFAT.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "fat" }));
        comboSelectFAT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSelectFATActionPerformed(evt);
            }
        });
        jToolBar2.add(comboSelectFAT);
        jToolBar2.add(jSeparator6);

        jLabel8.setText("Cluster Size: ");
        jLabel8.setToolTipText("");
        jToolBar2.add(jLabel8);

        comboClusterSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jToolBar2.add(comboClusterSize);
        jToolBar2.add(jSeparator14);

        jLabel9.setText("Max Size: ");
        jToolBar2.add(jLabel9);

        tfMaxSizeForSelectedFatAndCluster.setText("max");
        jToolBar2.add(tfMaxSizeForSelectedFatAndCluster);
        jToolBar2.add(jSeparator15);

        jLabel6.setText("Set Volume Label: ");
        jToolBar2.add(jLabel6);

        tfVolumeLabel.setText("usb");
        jToolBar2.add(tfVolumeLabel);
        jToolBar2.add(jSeparator4);

        btnToggleRunStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/go-green-krug-16.png"))); // NOI18N
        btnToggleRunStop.setText("Run format");
        btnToggleRunStop.setFocusable(false);
        btnToggleRunStop.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnToggleRunStop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                btnToggleRunStopItemStateChanged(evt);
            }
        });
        jToolBar2.add(btnToggleRunStop);
        jToolBar2.add(jSeparator8);

        jPanel3.add(jToolBar2, java.awt.BorderLayout.CENTER);

        jToolBar3.setBorder(javax.swing.BorderFactory.createTitledBorder("Status bar"));
        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);
        jToolBar3.setToolTipText("");
        jToolBar3.add(jSeparator7);

        btnAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/info-cyan-16.png"))); // NOI18N
        btnAbout.setText(" About");
        btnAbout.setFocusable(false);
        btnAbout.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });
        jToolBar3.add(btnAbout);
        jToolBar3.add(jSeparator3);

        btnQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/quit-16.png"))); // NOI18N
        btnQuit.setText("Quit");
        btnQuit.setFocusable(false);
        btnQuit.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnQuit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitActionPerformed(evt);
            }
        });
        jToolBar3.add(btnQuit);
        jToolBar3.add(jSeparator11);

        btnClearLog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/clear-yellow-16.png"))); // NOI18N
        btnClearLog.setText("Clear Log");
        btnClearLog.setFocusable(false);
        btnClearLog.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearLogActionPerformed(evt);
            }
        });
        jToolBar3.add(btnClearLog);

        jPanel3.add(jToolBar3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitActionPerformed
        int r = JOptionPane.showConfirmDialog(frame, "Really Quit ?", "Quit ?", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                //server.stop();
            } catch (NullPointerException ne) {
            }
            System.exit(0);
        }
    }//GEN-LAST:event_btnQuitActionPerformed

    private void btnToggleRunStopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_btnToggleRunStopItemStateChanged
        if (!StringUtils.isNumeric(tfSize.getText()) || tfName.getText().isEmpty() || tfType.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Some wrong parameters !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            return;
        }
        ImageIcon iconOn = new ImageIcon(getClass().getResource("/img/go-green-krug-16.png"));
        ImageIcon iconOf = new ImageIcon(getClass().getResource("/img/stop-16.png"));
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            if (running == true) {
                //server.stop();
                btnToggleRunStop.setIcon(iconOn);
                btnToggleRunStop.setText("Run server");
                btnToggleRunStop.setEnabled(true);
                setBooleanBtnTf(true);
                taLog.grabFocus();//.setFocusable(true);
                frame.setTitle(zagolovok + ", server stop");
                //j4log.log(Level.INFO, "pj-ftp-server stop");
                return;
            }
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            //try {
            //startServer(new String[0], tfPort.getText().trim(), tfUser.getText().trim(), tfPassw.getText().trim(), tfFolder.getText().trim(), comboListenIP.getSelectedItem().toString().trim());
            btnToggleRunStop.setIcon(iconOf);
            btnToggleRunStop.setEnabled(false);//.setText("Stop server");
            setBooleanBtnTf(false);
            //} catch (FtpException | FtpServerConfigurationException fe) {
            //JOptionPane.showMessageDialog(frame, "Some wrong !", "Error", JOptionPane.ERROR_MESSAGE);
            btnToggleRunStop.setSelected(false);
            //}
        }
    }//GEN-LAST:event_btnToggleRunStopItemStateChanged

    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        //changeLF();
        String msg = " USB FAT-format: "
                + "\n Free portable cross-platform"
                + "\n Pure Java FAT-format graphical utility. "
                + "\n Create by Roman Koldaev, "
                + "\n Saratov city, Russia. "
                + "\n mail: harp07@mail.ru "
                + "\n SourceForge: https://sf.net/u/harp07/profile/ "
                + "\n GitHub: https://github.com/harp077/ "
                + "\n Need JRE-1.8.";
        ImageIcon icone = new ImageIcon(getClass().getResource("/img/logo/0-usb-128.png"));
        JOptionPane.showMessageDialog(frame, msg, "About", JOptionPane.INFORMATION_MESSAGE, icone);
    }//GEN-LAST:event_btnAboutActionPerformed

    private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearLogActionPerformed
        try {
            new PrintWriter("log/app.log").close();
            taLog.setText("");
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(FatFormatMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        /*try (PrintWriter writer = new PrintWriter("log/app.log")) {
            writer.print("");
            writer.close();
            taLog.setText("");
            //taLog.repaint();
            //taLog.updateUI();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(PjFtpServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);        
        /*try (PrintWriter writer = new PrintWriter("log/app.log")) {
            writer.print("");
            writer.close();
            taLog.setText("");
            //taLog.repaint();
            //taLog.updateUI();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(FatFormatMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
    }//GEN-LAST:event_btnClearLogActionPerformed

    private void comboStoresStringsListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboStoresStringsListActionPerformed
        this.selectedStore = comboStoresStringsList.getSelectedItem().toString().trim();
        taLog.append("Selected Device: " + selectedStore + "\n");
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            if (store.toString().equals(selectedStore)) {
                selectedUSB = store;
            }
        }
        try {
            this.tfSize.setText("" + selectedUSB.getTotalSpace());
            this.sizeUsbBlockDevice=selectedUSB.getTotalSpace();
            taLog.append("Size Device: " + sizeUsbBlockDevice + "\n");
        } catch (IOException ex) {
            Logger.getLogger(FatFormatMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tfName.setText(selectedUSB.name());
        this.tfType.setText(selectedUSB.type());
        //jToolBar3.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }//GEN-LAST:event_comboStoresStringsListActionPerformed

    private void comboSelectFATActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSelectFATActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboSelectFATActionPerformed

    public static void main(String args[]) {
        /*try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FatFormatMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame = new FatFormatMain();
                frame.InstallLF();
                frame.setLF(frame);
                frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
                JOptionPane.setRootFrame(frame);
                frame.setSize(FW, FH);
                frame.setLocation(200, 200);
                frame.setResizable(true);
                frame.setVisible(true);
            }
        });
        //}
        //});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbout;
    public static javax.swing.JButton btnClearLog;
    private javax.swing.JButton btnQuit;
    public static javax.swing.JToggleButton btnToggleRunStop;
    public static javax.swing.JComboBox<String> comboClusterSize;
    public static javax.swing.JComboBox<String> comboSelectFAT;
    public static javax.swing.JComboBox<String> comboStoresStringsList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator14;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    public static javax.swing.JTextArea taLog;
    public static javax.swing.JTextField tfMaxSizeForSelectedFatAndCluster;
    public static javax.swing.JTextField tfName;
    public static javax.swing.JTextField tfOldFatType;
    public static javax.swing.JTextField tfSize;
    public static javax.swing.JTextField tfType;
    public static javax.swing.JTextField tfVolumeLabel;
    // End of variables declaration//GEN-END:variables
}
