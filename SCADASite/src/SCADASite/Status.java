/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.io.Serializable;

/**
 *
 * @author Shawn
 */
public class Status implements Serializable {
    
    public static final int CRITICAL = 3;
    public static final int WARNING = 2;
    public static final int NOTNORMAL = 1;
    public static final int NORMAL = 0;
    
    private int status = NORMAL;
    private boolean justChanged = false;
    
    public Status() {
        super();
    }

    public int getStatusCode() {
        return status;
    }

    public void setStatusCode(int status) {
        if(status == this.status)
            justChanged = false;
        else
            justChanged = true;
        
        this.status = status;
    }
    
    public boolean didJustChange() {
        return justChanged;
    }
    
    public boolean isCritical() {
        return status == CRITICAL;
    }
    
    public boolean isWarning() {
        return status == WARNING;
    }
    
    public boolean isNotNormal() {
        return status == NOTNORMAL;
    }
    
    public boolean isNormal() {
        return status == NORMAL;
    }
    
    public void setJustChanged(boolean bool) {
        justChanged = bool;
    }
}
