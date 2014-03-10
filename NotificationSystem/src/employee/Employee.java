/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employee;

import java.util.Calendar;
import util.Time;

/**
 *
 * @author Shawn
 */
public final class Employee implements Comparable<Employee>{
    
   
    
    private String name, pager, email; // pager should equal the ip or something to send to employee
    /*
     * The time the employee starts his/her shift.
     * Expressed in 24 hour format.
     * 8:30PM would be expressed as 19.50. 19th hour of the day; halfway through the hour.
     */
    private Time startTime, stopTime;
    private int priority;
    private int dayWorking; // all should equal one of Calendar.SUNDAY, etc.

    public Employee() {
        super();
    }
    
    public Employee(String name, String pager, String email, double startHour, double stopHour, int priority, int dayWorking) {
        super();
        this.name = name;
        this.email = email;
        this.pager = pager;
        this.startTime = new Time(startHour);
        this.stopTime = new Time(stopHour);
        this.dayWorking = dayWorking;
        this.priority = priority;
    }
    
    public boolean isCurrentlyWorking() {
        Calendar now = Calendar.getInstance();
        
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        
        double minuteInDec = (double)minute / 60.0; // convert minutes to decimal. 30 minutes converts to 0.5
        double adjustedHour = (double)hour + minuteInDec;
        
        if(adjustedHour >= startTime.getTimeAsDecimal() && adjustedHour <= stopTime.getTimeAsDecimal()) { //if the adjusted time is in between their hours
            if(dayWorking == dayOfWeek)
                return true;
        }
        
        return false;
    }
    

    @Override
    public int compareTo(Employee t) {
        return priority - t.priority;
    }

    public String getName() {
        return name;
    }
    
    public String getPager() {
        return pager;
    }

    public String getEmail() {
        return email;
    }
    
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getStopTime() {
        return stopTime;
    }

    public void setStopTime(Time stopTime) {
        this.stopTime = stopTime;
    }
    
    public int getDayWorking() {
        return dayWorking;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPager(String pager) {
        this.pager = pager;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setDayWorking(int dayWorking) {
        this.dayWorking = dayWorking;
    }
    
   
    public void goUpPriority() {
        priority--;
    }
    
    public void goDownPriority() {
        priority++;
    }
    
    public boolean hasEmail() {
        return email != null;
    }
    
    public boolean hasPager() {
        return pager != null;
    }
}
