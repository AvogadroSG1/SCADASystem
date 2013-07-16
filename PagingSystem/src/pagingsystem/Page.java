package pagingsystem;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Avogadro
 */
public class Page implements Runnable
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
    private boolean MxReady = false;
    private String buffer = "";
    private boolean sentMessage = false;
    private boolean pageSent;
    private String formedMsg;
    private long startTime;
    private int numTries;
    private String ip;
    private int port;
    private long currentTime;
    private Thread currentResponder;
    private Thread oldThread;
    private boolean sawp;
    
    public Page(String pagerID, String aMessage, String aIp, int aPort)
    {
        formedMsg = "" + STX + pagerID + CR + aMessage + CR + ETX;
        ip = aIp;
        port = aPort;
    }
    
    public void start() throws UnknownHostException, IOException
    {
        numTries = 0;
        connect();
    }
        
    private void connect() throws UnknownHostException, IOException
    {
        socket = new Socket(ip, port);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        sendCR();
        startTime = System.currentTimeMillis();
        sawp = false;
        if(currentResponder == null)
        {
            currentResponder = new Thread(this);
            currentResponder.start();
        }
       
    }  

    private void sendCR() throws IOException
    {
        if(!socket.isClosed())
            os.write(CR);
    }
    
    private void sendLoginAndMessage() throws IOException
    {
        String everything = "" + ESC + (char)0x050 + (char)0x47 + (char)0x31 +CR;
        os.write(everything.getBytes());
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
        }
        os.write((formedMsg + calculateChecksum(formedMsg)).getBytes());
        sentMessage = true;
    }
  
    private void logoff() throws IOException
    {
        if(!socket.isClosed())
        {
            os.write(("CR" + EOT + CR).getBytes());
            os.flush();
        }
        disconnect();
    }
    
    private void disconnect() throws IOException
    {
        socket.close();
    }
    
    private void reconnect() throws IOException, InterruptedException
    {
        startTime = System.currentTimeMillis();
        disconnect();
        Thread.sleep(5000);
        connect();
    }
    
    private void respond(String recieved) throws IOException, InterruptedException
    {
        if(recieved.contains("ID="))
            sendLoginAndMessage();
        else if(recieved.contains("[p"))
            sawp = true;
        else 
            sendCR();
    }
    
    public void run()
    {
        boolean loggedOff = false;
        
        while(!loggedOff)
        {
            currentTime = System.currentTimeMillis();
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
                }
            
            try
            {
                if (is == null)
                    continue;
                char temp = (char)is.read();
                buffer += temp;
                //System.out.println(temp);
                if(temp == CR || buffer.contains("ID="))
                {
                    respond(buffer);
                    buffer = "";
                }
                
                if(sentMessage  && temp == ACK)
                {
                    pageSent = true;
                    logoff();
                    loggedOff = true;
                }
                
            }
            catch(IOException e)
            {
                System.out.println(e.toString());
                Logger.getGlobal().log(Level.SEVERE, e.toString());
            } catch (InterruptedException ex) 
            {
                Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
}
