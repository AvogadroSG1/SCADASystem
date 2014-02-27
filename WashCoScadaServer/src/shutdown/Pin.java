/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shutdown;

/**
 *
 * @author Shawn
 */
public class Pin {
    
    private final String person, pin;
    
    public Pin(String personName, String aPin) {
        person = personName;
        pin = aPin;
    }

    public String getPersonName() {
        return person;
    }

    public String getPin() {
        return pin;
    }
    
    
}
