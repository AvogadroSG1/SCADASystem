/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servutil;

/**
 *
 * @author Shawn
 */
public abstract class ServerUtilities {
    
    public static String getBaseDirectory() {
        return System.getProperty("user.home") + "/.scada/";
    }
    
}
