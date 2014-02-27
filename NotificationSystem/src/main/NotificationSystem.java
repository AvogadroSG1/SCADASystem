/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import alert.AlertMonitoringSystem;
import email.EmailSystem;
import employee.EmployeeHandler;
import gui.NotificationSystemPanel;
import pagingsystem.PagingSystem;
import util.ServerProperties;

/**
 *
 * @author Shawn
 */
public class NotificationSystem {
    
    private ServerProperties props;
    
    private PagingSystem pagingSystem;
    private EmailSystem emailSystem;
    private AlertMonitoringSystem alertSystem;
    private EmployeeHandler employeeHandler;
    
    private NotificationSystemPanel panel;
    
    public NotificationSystem(ServerProperties props) {
        super();
        this.props = props;
        init();
        
        panel = new NotificationSystemPanel(this);
    }
    
    private void init() {
        employeeHandler = new EmployeeHandler();
        
        alertSystem = new AlertMonitoringSystem();
        
        pagingSystem = new PagingSystem(props);
        emailSystem = new EmailSystem(props);
        
        pagingSystem.setAlertMonitoringSystem(alertSystem);
        pagingSystem.setEmployeeHandler(employeeHandler);
        
        emailSystem.setAlertMonitoringSystem(alertSystem);
        emailSystem.setEmployeeHandler(employeeHandler);
    }
    
    public PagingSystem getPagingSystem() {
        return pagingSystem;
    }
    
    public EmailSystem getEmailSystem() {
        return emailSystem;
    }
    
    public AlertMonitoringSystem getAlertMonitoringSystem() {
        return alertSystem;
    }
    
    public EmployeeHandler getEmployeeHandler() {
        return employeeHandler;
    }
    
    public NotificationSystemPanel getNotificationSystemPanel() {
        return panel;
    }
}
