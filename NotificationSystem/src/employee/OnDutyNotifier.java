/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package employee;

import alert.Alert;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import main.NotificationSystem;

/**
 *
 * @author Shawn
 */
public class OnDutyNotifier {
    
    private final NotificationSystem notification;
    private final EmployeeHandler handler;

    private ScheduledExecutorService executor;
    
    public OnDutyNotifier(NotificationSystem notification, EmployeeHandler handler) {
        this.notification = notification;
        this.handler = handler;
    }
    
    public void startOnDutyNotifications() {
        for(Employee employee: handler.getAllEmployees()) {
            Calendar now = Calendar.getInstance();
            Calendar startShift = Calendar.getInstance();
            
            int startHour;
            int startMinute;
            
            startShift.set(Calendar.DAY_OF_WEEK, employee.getDayWorking());
            startShift.set(Calendar.HOUR, employee.getStartTime().getHour());
            startShift.set(Calendar.MINUTE, employee.getStartTime().getMinute());
            long millisBetween = startShift.add
            // make the delay one week so that 
            executor.scheduleWithFixedDelay(new NotifyEmployee(employee), initialDelay, delay, TimeUnit.MILLISECONDS);
        }
    }
    
    private class NotifyEmployee implements Runnable {

        private final Employee employee;

        public NotifyEmployee(Employee employee) {
            this.employee = employee;
        }
        
        @Override
        public void run() {
            Alert alert = new Alert(0, "You are now on duty");
            
            notification.notify(employee, alert);
        }
        
    }
}
