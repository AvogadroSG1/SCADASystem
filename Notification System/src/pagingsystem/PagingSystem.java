/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingsystem;

import alert.Alert;
import alert.AlertMonitoringSystem;
import employee.Employee;
import employee.EmployeeHandler;
import gui.EmployeePanel;
import gui.PagingProgressPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;
import java.util.logging.FileHandler;
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
public final class PagingSystem implements AlertListener {
    
    private PagingSystemPanel parent;
    private EmployeeHandler employeeHandler;
    private AlertMonitoringSystem ams;
    private PageAndVoiceProperties props;
    private PagingProgressPanel ppp;
    private PrintWriter pageLog;
    private static Logger log = Logger.getGlobal();
    
    private Stack<LogListener> logListeners = new Stack();
    
    public PagingSystem(PageAndVoiceProperties props) {
        super();
        this.props = props;
        
        String[] verbose = {"v"};
        dispatch(verbose);
        
        File pageFile = new File("pagelog.txt");
        try 
        {
            pageLog = new PrintWriter(pageFile);
        } catch (FileNotFoundException ex) 
        {
            Logger.getGlobal().log( Level.SEVERE, "Could not open page log file for writing.");
        }
        
        parent = new PagingSystemPanel(this);
        ppp = new PagingProgressPanel();
    }
    
    
    protected void setIPAddress(String address) {
        props.setPagerIP(address);
    }
    
    protected void setPort(String port) {
        props.setPagerPort(Integer.parseInt(port));
    }
    
    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }
    
    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }
    
    public void notifyAllLogListeners(String logText) {
        for(LogListener listener: logListeners) {
            listener.onLog(logText);
        }
    }
    
    protected void errorRecovery(Exception ex) {
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
            // do nothing
        } else if(chose.equals(CHANGE_IP)) {
            props.setPagerIP("");
        } else if(chose.equals(CHANGE_PORT)) {
            props.setPagerPort(-1);
        } else if(chose.equals(QUIT)) {
            int dialogResult = JOptionPane.showConfirmDialog(this.getPagingSystemPanel(), "Are you sure you want to quit?\n"
                    + "All active pages will be deleted.\n"
                    + "If any errors are sitll active, they will alert again when the program is reopened", "Are you sure?",JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION)
                System.exit(4);
            else
                errorRecovery(ex);
        }
    }
    
    @Override
    public void alertReceived(Alert alert) {
        page(alert);
    }
    
    public void page(Alert alert) {
        
        Employee[] employees = employeeHandler.getCurrentPrioritizedEmployees();
        
        int length = Math.min(employees.length, alert.getTimesPaged());
        
        Employee[] pageEmployee = new Employee[length];

        if(employees.length == 0) {
            notifyAllLogListeners("There are no employees on duty");
            return;
        }

        for(int i = 0; i < pageEmployee.length; i++) {
            pageEmployee[i] = employees[i];
        }


        for(Employee employee: pageEmployee) {
            Page page = new Page(this, employee, alert.getMessage(), props);
            boolean worked = false;
            do {
                try {
                    page.start(); 
                    worked = true;
                } catch (IOException ex) {
                    //errorRecovery(ex);
                    Logger.getGlobal().log(Level.SEVERE, null, ex);
                }
            }while(!worked);
            pageLog.println("Paged: " + employee.getName() + " with message " + alert.getMessage());
            pageLog.flush();
            notifyAllLogListeners("Paged: " + employee.getName() + " with message " + alert.getMessage());
            try { Thread.sleep(5000); } catch (InterruptedException ex) {}
        }
    }
            
    
    public PagingSystemPanel getPagingSystemPanel() {
        return parent;
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
            
            JScrollPane scroller = new JScrollPane(logArea);
            
            
            JPanel alertPanel = new JPanel(new GridLayout(2,2));
            
            alertPanel.add(ipLabel);
            alertPanel.add(changeIPButton);
            alertPanel.add(portLabel);
            alertPanel.add(changePortButton);
            
            contentPanel.add(alertPanel, BorderLayout.CENTER);
            
            this.add(scroller, BorderLayout.CENTER);
            this.add(contentPanel, BorderLayout.NORTH);
            
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
    
    public PagingProgressPanel getPagingProgressPanel() {
        return ppp;
    }
    
    static void dispatch(String[] args)
    {
        for(String s: args)
        {
            s = s.replaceAll("-", "");
            char command = s.charAt(0);
            
            switch (command)
            {
                case 'v':
                    log.setLevel(Level.ALL);
            try 
            {
                FileHandler fh = new FileHandler("pagelog.xml");
                log.addHandler(fh);
                log.info("Hai");
            } catch (IOException ex) 
            {
                Logger.getGlobal().info(ex.toString());
            } catch (SecurityException ex) 
            {
                Logger.getGlobal().info(ex.toString());
            }
            }
        }
        
        
    }
    
    public void setAlertMonitoringSystem(AlertMonitoringSystem ams) {
        if(ams != null) {
            ams.removeAlertListner(this);
        }
        
        this.ams = ams;
        
        if(ams != null)
            ams.addAlertListener(this);
    }
    
    public void setEmployeeHandler(EmployeeHandler employeeHandler) {
        this.employeeHandler = employeeHandler;
    }
    
}
