/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shutdown;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import servutil.ServerUtilities;

/**
 *
 * @author Shawn
 */
public abstract class ShutdownSecurity {
    
    public static Pin[] getPins() throws IOException {
        ArrayList<Pin> pins = new ArrayList();
        File pinFile = new File(ServerUtilities.getBaseDirectory()+ "/security/pins.csv");
        if(!pinFile.exists())
            pinFile.createNewFile();
        
        Scanner scanner = new Scanner(pinFile);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(",");
            Pin newPin = new Pin(tokens[0], tokens[1]);
            pins.add(newPin);
        }
        
        return pins.toArray(new Pin[pins.size()]);
    }
    
    
}
