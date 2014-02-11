/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import alert.AlertMonitoringSystem;
import employee.EmployeeHandler;
import gui.NotificationSystemPanel;
import pagingsystem.PagingSystem;
import util.PageAndVoiceProperties;

/**
 *
 * @author Shawn
 */
public class NotificationSystem {
    
    private PageAndVoiceProperties props;
    
    private PagingSystem pagingSystem;
    private AlertMonitoringSystem alertSystem;
    private EmployeeHandler employeeHandler;
    
    private NotificationSystemPanel panel;
    
    public NotificationSystem(PageAndVoiceProperties props) {
        super();
        this.props = props;
        init();
        
        panel = new NotificationSystemPanel(this);
    }
    
    private void init() {
        employeeHandler = new EmployeeHandler();
        
        alertSystem = new AlertMonitoringSystem();
        
        pagingSystem = new PagingSystem(props);
        pagingSystem.setAlertMonitoringSystem(alertSystem);
        pagingSystem.setEmployeeHandler(employeeHandler);
    }
    
    public PagingSystem getPagingSystem() {
        return pagingSystem;
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
