/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Shawn
 */
public abstract class Utilities {
    
    private Utilities() {
        
    }
    
    public static String getMainDirPath() {
        return System.getProperty("user.home") + "/.scada/";
    }
    
    public static String[] getDaysOfWeek() {
        String[] namesOfDayOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return namesOfDayOfWeek;
    }
    
    public static String timeFormat(double time) {
        int hours = (int) time;
        time -= hours;
        int minutes = (int) (time * 60.0);
        NumberFormat format = new DecimalFormat("00");
        String hoursText = format.format(hours);
        String minutesText = format.format(minutes);
        return "" + hoursText + ":" + minutesText;
    }
}
