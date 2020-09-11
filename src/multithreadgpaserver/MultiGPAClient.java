/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multithreadgpaserver;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiGPAClient {

/**
 *
 * @author jamesostmann
 */
    private DataInputStream fromServer = null;
    private DataOutputStream toServer = null;
    private Socket socket = null;
   
    public static void main(String[] args) {
        new MultiGPAClient(9989,new String[] {"4, A-, 3, A, 3, C+, 4, B, 3, 2.88, 46",
                                               "5, A, 3, B+, 4, A, 1, C, 3, A-, 3, 3.12, 75"});
    }
    public MultiGPAClient(int port,String[] messages) {
        try {
            socket = new Socket(InetAddress.getLocalHost(),port);
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            fromServer = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            toServer = new DataOutputStream(socket.getOutputStream());
            
            for(int i = 0; i < messages.length; i++) {
                
                System.out.println("Sending data to server...");
                toServer.writeUTF(messages[i]);
                
                String answer = fromServer.readUTF();
                String[] values = answer.split(", ");
                System.out.println("Semester GPA : " + values[0]);
                System.out.println("Cumulative GPA: " + values[1]);
                System.out.println("Total Credits: " + values[2]);
                
            }
            System.out.println("Sending stop to server...");
            toServer.writeUTF("Ok");            
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(MultiGPAClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultiGPAClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
}
