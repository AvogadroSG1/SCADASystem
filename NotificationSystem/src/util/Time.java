/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author Shawn
 */
public class Time {
    
    private int hour;
    private int minute;
    
    /*
        Accepts input as a hour in decimal format. 4.5 would be 4:30
    */
    public Time(double hourInDec) {
        hour = (int) hourInDec;
        double adjustedHour = hourInDec - hour;
        minute = (int) (adjustedHour * 60.0);
    }

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    public double getTimeAsDecimal() {
        double minuteDec = (double)minute / 60.0;
        double hourDec = (double)hour + minuteDec;
        
        return hourDec;
    }
    
    @Override
    public String toString() {
        return "" + hour + ":" + minute;
    }
}
