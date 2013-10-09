/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modem;

import gui.PagingGUI;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.PageAndVoiceProperties;


/**
 *
 * @author Shawn
 */
public class PageWithModem implements Runnable, ReadListener {
    
    private ModemConnector mc;
    private PagingPlug plug;
    
    private PageAndVoiceProperties props;
    
    private PagingGUI pagingGUI;
    
    private JobAckCodeGenerator jacg = new JobAckCodeGenerator();
    
    private ServerSocket pagingModuleServer;
    private Socket pagingModuleSocket;
    
    private Logger log = Logger.getGlobal();
    
    private boolean active = false;
    
    
    
    public PageWithModem() {
        super();
        log.log(Level.INFO, "-------Making New Page and Voice Properties-------");
        props = new PageAndVoiceProperties();
        log.log(Level.INFO, "-------Properties Made-------");
        makeGUI();
    }

    private void makeGUI()
    {
        try {
            log.log(Level.INFO, "-------Making The GUI!-------");
            pagingGUI = new PagingGUI(props);
            log.log(Level.INFO, "-------GUi Created!-------");
            pagingGUI.addTab("Phone", new PageAndModemPanel(this));
        } catch (IOException ex) {
            Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
    
    private void initModem() {
        try {
            String ip = props.getModemIP();
            String port = ""+props.getModemPort();
            mc = new ModemConnector(ip, port);
            log.log(Level.INFO, "New Modem Connector created");
            mc.addReadListener(this);
            log.log(Level.INFO, "Made new Read Listener");
            mc.start();
            log.log(Level.INFO, "Started MC");
        } catch (IOException ex) {
            Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Voice Modem ip and port incorrect");
        }
    }
    
    public void startPage(int scadaID, String message) {
        try {
            plug.startPage(scadaID, message);
            log.log(Level.INFO, "started page");
        } catch (IOException ex) {
            fix();
            startPage(scadaID, message);
        }
    }
            
    public void stopPage(int scadaID) {
        try {
            plug.stopPage(scadaID);
        } catch (IOException ex) {
            fix();
            stopPage(scadaID);
        }
    }
    
    public void acknowledgePage(int scadaID) {
        try {
            plug.acknowledgePage(scadaID);
        } catch (IOException ex) {
            fix();
            acknowledgePage(scadaID);
        }
    }
    
    public int getStatus(int scadaID) {
        try {
            return plug.getStatus(scadaID);
        } catch (IOException ex) {
            fix();
            return getStatus(scadaID);
        }
    }
    
    public String getAllActivePages() {
        try {
            return plug.getAllActivePages();
        } catch (IOException ex) {
            fix();
            return getAllActivePages();
        }
    }
    
    public void stopAllRunningPages() {
        try {
            plug.stopAllRunningPages();
        } catch (IOException ex) {
            fix();
            stopAllRunningPages();
        }
    }
    
    private void resetPagingModule() {
        stopPagingModule();
        startPagingModule();
    }
    
    private void startPagingModule() {
        new Thread(this).start();
    }
    
    private void stopPagingModule() {
        if(pagingModuleServer != null && !pagingModuleServer.isClosed()) {
            try {
                plug.stopAllRunningPages();
                pagingModuleServer.close();
                pagingModuleServer = null;
            } catch (IOException ex) {
                Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(pagingModuleSocket != null && !pagingModuleSocket.isClosed()) {
            try {
                pagingModuleSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
          
    public synchronized PagingGUI getPagingGUI() {
        return pagingGUI;
    }
    
    private void resetModemConnector() {
        stopModemConnector();
        startModemConnector();
    }
    
    private void stopModemConnector() {
        mc = null;
    }
    
    private void startModemConnector() {
        initModem();
    }
    
    public synchronized void start() {
        log.log(Level.INFO, "Preping the business for opening.");
        startModemConnector();
        log.log(Level.INFO, "Modem connector started");
        startPagingModule();
        active = true;
    }
    
    public void stop() {
        stopModemConnector();
        stopPagingModule();
        active = false;
    }
    
    public boolean isActive()
    {
        return active;
    }
    
    @Override
    public void run() {
        try{
            //pagingSystem = new PagingSystem();
            int port = 7655;
            log.log(Level.INFO, "Starting ServerSocket");
            pagingModuleServer = new ServerSocket(port);
            log.log(Level.INFO, "We're open for business!");
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(null, "Port 7655 is needed for this application to run.\n"
                    + "Stop the other process running on this port or contact developers.");
            return;
        }
        try {
            while(!pagingModuleServer.isClosed()) {
                pagingModuleSocket = pagingModuleServer.accept();
                try{
                    plug = new PagingPlug(pagingModuleSocket);
                    log.log(Level.INFO, "Created the paging plug!!!!");
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error opening streams, please try again");
                    Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(null, "Paging System turned off");
        }
    }
    
    private void fix() {
            JOptionPane.showMessageDialog(null, "The paging module needs to connect to: 127.0.0.1:7655. Contact network administrator for further help.");
            System.exit(42);
    }
    
    
    public PagingPlug getPagingPlug() {
        return plug;
    }

    @Override
    public void onRead(final String pinText) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if(plug == null) {
                        JOptionPane.showMessageDialog(null, "Paging system not connected. Please connect and click OK");
                        onRead(pinText);
                        return;
                    }
                    int pin = Integer.parseInt(pinText);
                    int scadaID = jacg.getScadaID(pin);
                    plug.acknowledgePage(scadaID);
                    jacg.ackScadaID(scadaID);
                } catch (IOException ex) {
                    fix();
                    onRead(pinText);
                }
            }
        };
        new Thread(r).start();
    }
    
    private class PagingPlug  {
        
        private InputStream is;
        private OutputStream os;
        
        public PagingPlug(Socket socket) throws IOException {
            super();
            is = socket.getInputStream();
            os = socket.getOutputStream();
        }
        
        protected void startPage(int scadaID, String message) throws IOException {
            if(jacg.getAckCode(scadaID) != -2)  {
                System.out.println("Already paging about " + scadaID);
                System.out.println("Please contact Specialized Programming LLC");
                return;
            }
            
            int ackCode = jacg.generateAckCode(scadaID);
            log.log(Level.INFO, "Generated Ack Code");
            String compose = "ST " + scadaID + " " + message + " ACKCODE:" + ackCode;
            os.write(compose.getBytes());
            os.flush();
            //LoggingSystem.getLoggingSystem().alertAllLogListeners("Page sent: scadaID-" + scadaID + " message-" + message);
        }
        
        protected void acknowledgePage(int scadaID) throws IOException {
            String compose = "ACK " + scadaID;
            os.write(compose.getBytes());
            os.flush();
            //LoggingSystem.getLoggingSystem().alertAllLogListeners("Acknowledgement received: scadaID-" + scadaID);
        }
        
        protected void stopPage(int scadaID) throws IOException {
            String compose = "SP " + scadaID;
            os.write(compose.getBytes());
            os.flush();
        }
        
        protected int getStatus(int scadaID) throws IOException {
            String compose = "S " + scadaID;
            os.write(compose.getBytes());
            os.flush();
            return Integer.parseInt(readBuffer());
        }
        
        protected String getAllActivePages() throws IOException {
            String compose = "AAP";
            os.write(compose.getBytes());
            os.flush();
            return readBuffer();
        }
        
        protected void stopAllRunningPages() throws IOException {
            String compose = "SPA";
            os.write(compose.getBytes());
            os.flush();
        }
        
        private String readBuffer() throws IOException {
            String buffer = "";
            
            while(is.available() > 0) {
                int read = is.read();
                if(read == -1) {
                    throw new IOException("The connection was broken.");
                }
                buffer += (char) read;
            }
            
            return buffer;
        }
    }
    
    private class JobAckCodeGenerator {
        
        private Random random;
        private ArrayList<AckCode> activeCodes;
        
        public JobAckCodeGenerator() {
            super();
            random = new Random();
            activeCodes = new ArrayList();
        }
        
        public int generateAckCode(int scadaID) {
            String codeText = "";
            int ran;
            do {
                for(int i = 0; i < 4; i++) {
                    int randomInt = random.nextInt(9); //0-8
                    randomInt++; //1-9
                
                    codeText += randomInt;
                }
                
                ran = Integer.parseInt(codeText);
            } while(!randomUsed(ran) && codeText.length() <= 4);
            
            
            //now we have a random int that isn't used yet
            AckCode code = new AckCode(scadaID, ran);
            activeCodes.add(code);
            return code.getAckCode();
        }
        
        
        private boolean randomUsed(int randomInt) {
            for(AckCode code: activeCodes) {
                if(code.getAckCode() == randomInt)
                    return true;
            }
            return false;
        }
        
        public int getAckCode(int scadaID) {
            for(AckCode code: activeCodes) {
                if(code.scadaID == scadaID)
                    return code.getAckCode();
            }
            
            return -2;
        }
        
        public int getScadaID(int ackCode) {
            for(AckCode code: activeCodes) {
                if(code.ackCode == ackCode)
                    return code.scadaID;
            }
            
            return -2;
        }
        
        /*
         * Acknoledge the scadaID
         */
        public void ackScadaID(int scadaID) {
            for(AckCode code: activeCodes) {
                if(code.getScadaID() == scadaID) {
                    activeCodes.remove(code);
                    return;
                }
            }
        }
        
        private class AckCode {
            private final int scadaID;
            private final int ackCode;
            
            public AckCode(int scadaID, int ackCode) {
                super();
                this.scadaID = scadaID;
                this.ackCode = ackCode;
            }

            public int getAckCode() {
                return ackCode;
            }

            public int getScadaID() {
                return scadaID;
            }
        }
    }
    
    private void configure(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Configure", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new PageAndModemPanel(this));
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private class PageAndModemPanel extends JPanel {
        
        private JLabel modemIPLabel = new JLabel(), modemPortLabel = new JLabel();
        private JButton changeMIPButton = new JButton("Change"), changeMPButton = new JButton("Change");
        
        private PageWithModem pwm;
        
        public PageAndModemPanel(PageWithModem pwm) {
            super(new GridLayout(3,2,10,10));
            this.pwm = pwm;
            init();
        }
        
        private void init() {
            this.setBorder(new EmptyBorder(10,10,10,10));
            
            changeMIPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    props.setModemIP("");
                    resetModemConnector();
                    updateLabels();
                }
            });
            
            changeMPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    props.setModemPort(-1);
                    resetModemConnector();
                    updateLabels();
                }
            });
            
            this.add(modemIPLabel);
            this.add(changeMIPButton);
            
            this.add(modemPortLabel);
            this.add(changeMPButton);
            
            updateLabels();
        }
        
        private void updateLabels() {
            if(pwm != null) {
                modemIPLabel.setText("Phone Modem IP: " + props.getModemIP());
                modemPortLabel.setText("Phone Modem Port: " + props.getModemPort());
            }
        }
    }
    
}

