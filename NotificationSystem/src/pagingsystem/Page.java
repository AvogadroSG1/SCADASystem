package pagingsystem;


import employee.Employee;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import util.PageAndVoiceProperties;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Avogadro
 */
public class Page
{
    private InputStream is = null;
    private OutputStream os = null;
    private Socket socket = null;
    private final char ESC = 0x1B;
    private final char CR  = 0x0D;
    private final char EOT = 0x04;
    private final char STX = 0x02;
    private final char ETX = 0x03;
    private final char ACK = 0x06;
    private String buffer = "";
    private boolean sentMessage = false;
    private boolean pageSent;
    private String formedMsg;
    private long startTime;
    private int numTries;
    private long currentTime;
    private boolean sawp;
    private PagingSystem ps;
    private Employee employee;
    private PageAndVoiceProperties props;
    private boolean loggedOff;
    
    public Page(PagingSystem ps, Employee employee, String aMessage, PageAndVoiceProperties props)
    {
        this.ps = ps;
        this.employee = employee;
        formedMsg = "" + STX + employee.getPager() + CR + aMessage + CR + ETX;
        this.props = props;
    }
    
    public void start() throws UnknownHostException, IOException
    {
        numTries = 0;
        setPagingProgressText("Sending page to " + employee.getName());
        setPagingProgress(0);
        run();
    }
        
    private void connect() throws UnknownHostException, IOException
    {
        socket = new Socket(props.getPagerIP(), props.getPagerPort());
        is = socket.getInputStream();
        os = socket.getOutputStream();
        sendCR();
        startTime = System.currentTimeMillis();
        sawp = false;
        
        setPagingProgress(25);
        
        alertAllLogListeners("Connected to paging server");
        
        //this.run();
       loggedOff = false;
    }  

    private void sendCR() throws IOException
    {
        if(!socket.isClosed())
            write(""+CR);
    }
    
    private void sendLoginAndMessage() throws IOException
    {
        String everything = "" + ESC + (char)0x050 + (char)0x47 + (char)0x31 +CR;
        write(everything);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
        }
        write((formedMsg + calculateChecksum(formedMsg)));
        sentMessage = true;
        alertAllLogListeners("Logged on and sent message");
    }
  
    private void logoff() throws IOException
    {
        alertAllLogListeners("Logging off paging server");
        if(!socket.isClosed())
        {
            //write(("CR" + EOT + CR));
            write("" + EOT + CR);
            //os.flush();
        }
    }
    
    private void disconnect() throws IOException
    {
        alertAllLogListeners("Disconnecting");
        socket.close();
        loggedOff = true;
    }
    
    private void reconnect() throws IOException
    {
        alertAllLogListeners("Starting reconnection");
        startTime = System.currentTimeMillis();
        disconnect();
        
        try { 
            Thread.sleep(5000);
        } catch(Exception ex) {}
        
        connect();
    }
    
    private void respond(String recieved) throws IOException
    {
        if(recieved.contains("ID="))
            sendLoginAndMessage();
        else if(recieved.contains("[p"))
            sawp = true;
        else 
            sendCR();
    }
    
    public void run()  throws UnknownHostException, IOException
    {
        while(!pageSent) {
                setPagingProgress(0);
                
                connect();

                WatchdogThread watchdog = new WatchdogThread();
                watchdog.start();

                try {
                    while(!loggedOff)
                    {
                        /*currentTime = System.currentTimeMillis();
                        if(currentTime - startTime > 5000)
                            try 
                            {
                                numTries++;
                                reconnect();
                            } catch (IOException ex) 
                            {
                            Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) 
                            {
                            Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
                            }*/

                        if (is == null)
                                continue;

                        int read = is.read();
                        if(read == -1)
                            throw new IOException("Inputstream was closed");

                        try
                        {
                            char temp = (char)read;

                            buffer += temp;
                            //System.out.println(temp);
                            if(temp == CR || buffer.contains("ID="))
                            {

                                setPagingProgress(50);

                                respond(buffer);
                                buffer = "";
                            }

                            if(sentMessage  && temp == ACK)
                            {
                                alertAllLogListeners("Page succesfully sent");
                                setPagingProgress(75);

                                pageSent = true;
                                logoff();
                                loggedOff = true;
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                Date date = new Date();
                                Logger.getGlobal().log(Level.SEVERE, "Page: {0} Sent. " + dateFormat.format(date), formedMsg);
                            }

                        }
                        catch(IOException e)
                        {
                            disconnect(); // if it gives any kind of IOException, disconnect
                            
                            System.out.println(e.toString());
                            Logger.getGlobal().log(Level.SEVERE, e.toString());
                        } 
                    }

                } catch(IOException ex) {
                    // the watchdog closed the input stream
                    // the loop will run again
                    alertAllLogListeners("Watchdog closed stream, retrying");
                }
                
                watchdog.interrupt();
            
        }
        
        setPagingProgress(100);
        setPagingProgressText("No running pages");
        System.out.println("All should be well");
    }
    
    private String calculateChecksum(String toSend) {
        
        char[] bobints = toSend.toCharArray();
        int total = 0;
        
        for(char c : bobints)
        {
            total += c;
        }
        
        total %=4096;
        
        String hexString = Integer.toHexString(total).toUpperCase();
        
        while(hexString.length() < 3)
            hexString = "0" + hexString;

        int[] hexPlaces = new int[3];
        
        for(int i = 0; i < hexPlaces.length; i++)
        {
            hexPlaces[i] = Integer.parseInt(hexString.substring(i, i+1), 16) + 0x30;
        }
        
        String checkSum = "";
        
        for(int digit : hexPlaces)
        {
            checkSum += (char) digit;
        }

        return checkSum;
    }
    
    
    public boolean finished() {
        return pageSent;
    }
    
    private void setPagingProgressText(String text) {
        if(ps != null)
            ps.getPagingProgressPanel().getLabel().setText(text);
    }
    
    private void setPagingProgress(int progress) {
        if(ps != null)
            ps.getPagingProgressPanel().getProgressBar().setValue(progress);
    }
    
    private void alertAllLogListeners(String text) {
        if(ps != null) 
            ps.notifyAllLogListeners(text);
    }
    
    
    private class WatchdogThread extends Thread {
        
        
        @Override
        public void run() {
            try{
                Thread.sleep(5000);
                disconnect(); // this will close the inputstream and result in the above thread being restarted
            } catch(InterruptedException ex) {
                // the page is finished
            } catch(IOException ex) {
                //ps.errorRecovery(ex); let the thread deal with it
            }
            
        }
    }
    
    private void write(String string) throws IOException {
        /*for(byte byt : string.getBytes()) {
            alertAllLogListeners("" + (int)byt);
        }*/
        os.write(string.getBytes());
        os.flush();
    }
}
