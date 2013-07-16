/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingsystem;

import alert.Alert;
import alert.AlertMonitoringSystem;
import employee.Employee;
import employee.EmployeeHandler;
import employee.gui.EmployeePanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.AlertListener;
import util.LogListener;
import util.PageAndVoiceProperties;
import util.UpdateListener;


/**
 *
 * @author Shawn
 */
public final class PagingSystem implements AlertListener, UpdateListener {
    

    private JPanel parent;
    private AlertMonitoringSystem ams;
    private EmployeeHandler eh;
    private Socket socket;
    private OutputStream os;
    private InputStream is;
    private PageAndVoiceProperties props;
    
    private Stack<UpdateListener> updateListeners = new Stack();
    private Stack<LogListener> logListeners = new Stack();
    
    public PagingSystem(PageAndVoiceProperties props) throws IOException {
        super();
        
        eh = new EmployeeHandler();
        
        addUpdateListener(this);
     
        updateSocket();
        
        ams = new AlertMonitoringSystem();
        
        ams.addAlertListener(this);
        
        parent = new PagingSystemPanel(this);
    }
    
    /*
    private synchronized void sendPage(Page page) {
        try {
            // Send message
            System.out.println("Start sendPage to server");
            if(os == null) {
                throw new IOException("Unexpected error sending message");
            }
            System.out.println("Here's the page: " + page.toString());
            System.out.println("Here's the checksum " + page.getCheckSum());
            //os.write("\r".getBytes());
            os.write(page.toString().getBytes());
            os.flush();
            System.out.println("Wrote to WashCo Paging Server");
            String buffer = readBuffer();
            System.out.println("readBuffer OK");
            System.out.println(buffer);
            notifyAllLogListeners("Paged " + page.getEmployee().getName());
            
        } catch(IOException ex) {
            Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            errorRecovery(ex);
            sendPage(page);
        }
        System.out.println("Finished Sending Page");
    }*/
    
    private String readBuffer() throws IOException {
        String buffer= "";
        
        do {
            int i = is.read();
            if(i == -1)
                throw new IOException("The connection was broken.");

            buffer += (char) i;
        } while(is.available() > 0);
        
        return buffer;
    }
    
    @Override
    public void alertReceived(Alert alert) {
        PageThread thread = new PageThread(this, alert);
        thread.start();
    }
    
    protected void setIPAddress(String address) {
        props.setPagerIP(address);
        
        alertAllUpdateListeners();
    }
    
    protected void setPort(String port) {
        props.setPagerPort(Integer.parseInt(port));
        
        alertAllUpdateListeners();
    }
    
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }
    
    public void removeUpdateListner(UpdateListener listener) {
        updateListeners.remove(listener);
    }
    
    private void alertAllUpdateListeners() {
        for(UpdateListener listener: updateListeners) {
            listener.onUpdate();
        }
    }
    
    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }
    
    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }
    
    private void notifyAllLogListeners(String logText) {
        for(LogListener listener: logListeners) {
            listener.onLog(logText);
        }
    }

    @Override
    public void onUpdate() {
        updateSocket();
    }
    
    private void updateSocket() {
        try {
            if(socket != null)
                try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(is != null)
                try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(os != null)
                try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            String ip = props.getPagerIP();
            int port = props.getPagerPort();
            socket = new Socket();
            System.out.println(ip);
            System.out.println(port);
            socket.connect(new InetSocketAddress(ip, port), 1000);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            
            //LOGON
            //get attention
            //os.write("\r\r\r\r\r".getBytes());
            //os.flush();

            os.write("\r".getBytes());
            char ESC = 0x1B;
            char CR  = 0x0D;
            String everything = "" + CR + ESC + (char)0x050 + (char)0x47 + (char)0x31 +CR;
            os.write(everything.getBytes());
            os.flush();
            String see = readBuffer();

            
            // lets just assume for now the logon is accepted
            System.out.println(readBuffer());
            System.out.println("If [p, ready for pages!");
            
        } catch (IOException ex) {
            Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 

            errorRecovery(ex);
        }
    }
    
    private void errorRecovery(Exception ex) {
        final String RETRY = "Retry";
        final String CHANGE_IP = "Change IP";
        final String CHANGE_PORT = "Change Port";
        final String QUIT = "Quit";

        String[] options = {QUIT, CHANGE_PORT, CHANGE_IP, RETRY};

        int choseInt = JOptionPane.CLOSED_OPTION;
        while(choseInt == JOptionPane.CLOSED_OPTION) {
            choseInt = JOptionPane.showOptionDialog(parent, "Paging Server Connection Error\n"+ex.getMessage(), "Error Recovery", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, RETRY);
        }

        String chose = options[choseInt];
        if(chose.equals(RETRY)) {
            updateSocket();
        } else if(chose.equals(CHANGE_IP)) {
            setIPAddress("");
        } else if(chose.equals(CHANGE_PORT)) {
            setPort("");
        } else if(chose.equals(QUIT))
            System.exit(4);
    }
    
    public JPanel getPagingSystemPanel() {
        return parent;
    }
    
    private final int FIFTEEN = 15 * 60 * 1000;
    private final int TOTALTIME = 3 * 60 * 60 * 1000;
    
    private class PageThread extends Thread {

        private final PagingSystem ps;
        private final Alert alert;
        
        public PageThread(PagingSystem ps, Alert alert) {
            super();
            this.ps = ps;
            this.alert = alert;
        }
        
        @Override
        public void run() {
            Employee[] employees = eh.getCurrentPrioritizedEmployees();
            ArrayList<Employee> cascade = new ArrayList();
            
            if(employees.length == 0) {
                ps.notifyAllLogListeners("There are no employees on duty");
                return;
            }
            
            int index = 0;
            
            while(!alert.isAcknowledged()) { // person with the lowest rank (kinda like golf) goes first
                if(cascade.size() < employees.length) {
                    cascade.add(employees[index]);
                    index++;
                }
                
                for(Employee employee: cascade) {
                    Page page = new Page(employee.getPager(), alert.getMessage(), props.getPagerIP(), props.getPagerPort());
                    try {
                        
                        page.start();
                        while(!page.finished()) {
                            Thread.sleep(50);
                        }
                    } catch (Exception ex) {
                        errorRecovery(ex);
                    } 
                    
                }
                hold(FIFTEEN);
                }
            }
        }
        
        private void hold(int time) {
            try {
                System.out.println("Going to sleep");
                Thread.sleep(time); // 15 minutes
                System.out.println("I woke up!");
            } catch (InterruptedException ex) {
                Logger.getLogger(PagingSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        
    }
    
    public class PagingSystemPanel extends JPanel implements UpdateListener, LogListener {

        private PagingSystem ps;
        
        private JTextArea logArea;
        private JLabel ipLabel, portLabel;
        private JButton changeIPButton, changePortButton;
        
        protected PagingSystemPanel(PagingSystem aThis) {
            super();
            ps = aThis;
            init();
        }
        
        private void init() {
            this.setBorder(new EmptyBorder(10,10,10,10));
            this.setLayout(new BorderLayout());
            
            JPanel contentPanel = new JPanel(new BorderLayout());
            
            ipLabel = new JLabel("lol");
            portLabel = new JLabel("lol");
            setIPLabelText();
            setPortLabelText();
            
            changeIPButton = new JButton("Change IP");
            changeIPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    props.setPagerIP("");
                    setIPLabelText();
                }
            });
            
            changePortButton = new JButton("Change Port");
            changePortButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    props.setPagerPort(-1);
                    setPortLabelText();
                }
            });
            
            logArea = new JTextArea();
            logArea.setEditable(false);
            
            JPanel alertPanel = new JPanel(new GridLayout(2,2));
            
            alertPanel.add(ipLabel);
            alertPanel.add(changeIPButton);
            alertPanel.add(portLabel);
            alertPanel.add(changePortButton);
            
            contentPanel.add(alertPanel, BorderLayout.CENTER);
            
            this.add(logArea, BorderLayout.CENTER);
            this.add(contentPanel, BorderLayout.NORTH);
            
            ps.addUpdateListener(this);
            ps.addLogListener(this);
            
        }
        
        private void setIPLabelText() {
            ipLabel.setText("IP: " + ps.props.getPagerIP());
        }
        
        private void setPortLabelText() {
            portLabel.setText("Port: " + ps.props.getPagerPort());
        }
        
        @Override
        public void onUpdate() {
            setIPLabelText();
            setPortLabelText();
        }
        
        @Override
        public void onLog(String logText) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(logText + " on " + formattedDate + "\n");
        }
    }
    
    public JPanel getAlertMonitorPanel() {
        return ams.getAlertMonitoringPanel();
    }
    
    public EmployeePanel getEmployeePanel() {
        return eh.getEmployeePanel();
    }
}
