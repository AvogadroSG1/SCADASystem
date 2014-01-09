/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alert;

import java.util.Calendar;

/**
 *
 * @author Shawn
 */
public class Alert {
    
    
    private final int jobID;
    private final String message;
    
    private boolean acknowledged = false;
    private Calendar nextAlertTime;
    private int timesPaged = 0;
    
    public Alert(int jobID, String message) {
        super();
        
        this.jobID = jobID;
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void acknowledge() {
        acknowledged = true;
    }
    
    public boolean isAcknowledged() {
        return acknowledged;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof Alert) {
            Alert a = (Alert) o;
            if(a.jobID == jobID)
                return true;
        }
        
        return false;
    }
    
    public String toString() {
        return "" + message;
    }

    public int getJobID() {
        return jobID;
    }

    void setNextAlertTime(Calendar instance) {
        this.nextAlertTime = instance;
    }

    boolean isReadyToAlert() {
        Calendar now = Calendar.getInstance();
        if(nextAlertTime.getTimeInMillis() < now.getTimeInMillis()) { // if current time is past the time the page was supposed to send
            return true;
        }
        
        return false;
    }
    
    public int getTimesPaged() {
        return timesPaged;
    }
    
    public void incrementTimesPaged() {
        timesPaged++;
    }
}
