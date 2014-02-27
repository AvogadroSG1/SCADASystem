/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package email;

import alert.Alert;
import alert.AlertMonitoringSystem;
import employee.Employee;
import employee.EmployeeHandler;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Stack;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import util.AlertListener;
import util.LogListener;
import util.ServerProperties;

/**
 *
 * @author Shawn
 */
public class EmailSystem implements AlertListener{
    
    private static final String DEF_FROM_ADDRESS = "noreply@washco-md.net";
    private static final String DEF_SMTP_SERVER = "smtp.washco-md.net";
    
    private ServerProperties props;
    private EmailSystemPanel parent;
    private EmployeeHandler employeeHandler;
    private AlertMonitoringSystem ams;
    
    public EmailSystem(ServerProperties props) {
        super();
        
        this.props = props;
        parent = new EmailSystemPanel(this);
    }
    
    public void sendEmail(Alert alert, Employee employee) {
        
        if(!employee.hasEmail())
            return; // if the employee doesn't have an email... then don't email him
        
        // Recipient's email ID needs to be mentioned.
        String to = employee.getEmail();

        // Sender's email ID needs to be mentioned
        String from = props.getFromAddress();

        // Assuming you are sending email from localhost
        String host = props.getSMTPServer();

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try{
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("SCADA Alert System! Critical alert #" + alert.getJobID());

            // Send the actual HTML message, as big as you like
            message.setContent("<h1>This is actual message</h1>",
                                "text/html" );

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
            
            notifyAllLogListeners("Sent email to " + employee.getEmail());
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
    
    private Stack<LogListener> logListeners = new Stack();
    
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
    
    public EmailSystemPanel getEmailSystemPanel() {
        return parent;
    }

    @Override
    public void alertReceived(Alert alert) {
        Employee[] employees = employeeHandler.getCurrentPrioritizedEmployees();
        int length = Math.min(alert.getTimesPaged(), employees.length);
        
        if(employees.length == 0) {
            notifyAllLogListeners("There are no employees on duty");
            return;
        }
        
        for(int i = 0; i < length; i++) {
            sendEmail(alert, employees[i]);
        }
    }
    
    public class EmailSystemPanel extends JPanel implements LogListener {

        private EmailSystem es;
        
        private JTextArea logArea;
        private JLabel ipLabel, portLabel;
        private JButton changeIPButton, changePortButton;
        
        protected EmailSystemPanel(EmailSystem aThis) {
            super();
            es = aThis;
            init();
        }
        
        private void init() {
            this.setBorder(new EmptyBorder(10,10,10,10));
            this.setLayout(new BorderLayout());
            
            JPanel contentPanel = new JPanel(new BorderLayout());
            
            ipLabel = new JLabel("lol");
            portLabel = new JLabel("lol");
            setSMTPLabelText();
            setFromLabelText();
            
            changeIPButton = new JButton("Change SMTP Server");
            changeIPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    props.setSMTPServer("");
                    setSMTPLabelText();
                }
            });
            
            changePortButton = new JButton("Change From Address");
            changePortButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    props.setFromAddress("");
                    setFromLabelText();
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
            
            es.addLogListener(this);
            
        }
        
        private void setSMTPLabelText() {
            ipLabel.setText("SMTP Server: " + props.getPagerIP());
        }
        
        private void setFromLabelText() {
            portLabel.setText("From Email Address: " + props.getFromAddress());
        }
        
        @Override
        public void onLog(String logText) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(logText + " on " + formattedDate + "\n");
        }
    }
    
        
    public void setEmployeeHandler(EmployeeHandler eh) {
        employeeHandler = eh;
    }
    
    public void setAlertMonitoringSystem(AlertMonitoringSystem ams) {
        if(ams != null) {
            ams.removeAlertListner(this);
        }
        
        this.ams = ams;
        
        if(ams != null)
            ams.addAlertListener(this);
    }
}
