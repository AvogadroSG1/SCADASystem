/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dummy;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Shawn
 */
public class Test {
    
    public static void main(String[] args) {
        JDialog uh = errorRecovery(new Exception("Uh oh"));
        uh.setVisible(true);
    }
    
    protected static JDialog errorRecovery(Exception ex) {
        final String CHANGE_IP = "Change IP";
        final String CHANGE_PORT = "Change Port";
        final String QUIT = "Quit";

        JButton changeIPButton = new JButton("Change IP");
        
        JButton changePortButton = new JButton("Change Port");
        
        JButton quitButton = new JButton("Quit");
        
        Object[] options = {quitButton, changePortButton, changeIPButton};
        return new JOptionPane("PagingS", JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, changeIPButton).createDialog(null, "Uh oh");
        
/*
        String chose = options[choseInt];
        if(chose.equals(RETRY)) {
            // do nothing
        } else if(chose.equals(CHANGE_IP)) {
            props.setPagerIP("");
        } else if(chose.equals(CHANGE_PORT)) {
            props.setPagerPort(-1);
        } else if(chose.equals(QUIT)) {
            int dialogResult = JOptionPane.showConfirmDialog(this.getPagingSystemPanel(), "Are you sure you want to quit?\n"
                    + "All active pages will be deleted.\n"
                    + "If any errors are sitll active, they will alert again when the program is reopened", "Are you sure?",JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION)
                System.exit(4);
            else
                errorRecovery(ex);
        }*/
    }
}
