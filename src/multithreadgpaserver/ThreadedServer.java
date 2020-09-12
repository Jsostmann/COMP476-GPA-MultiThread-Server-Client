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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jamesostmann
 */
public class ThreadedServer {
    
    private ServerSocket server;
    private static final HashMap<String, Double> GPA_TABLE;
    
    public static void main(String[] args) {
        new ThreadedServer(9989);
    }
    
    static {
        GPA_TABLE = new HashMap<>();
        initTable();
    }

    public ThreadedServer(int port) {

        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Starting server with " + cores + " cores");

        try {
            server = new ServerSocket(port);

            while (true) {

                Socket socket = server.accept();
                InetAddress ip = socket.getInetAddress();
                
                System.out.println("Established connection with new client at " + ip.getHostAddress());
                
                Thread gpaThread = new Thread(new GPAThread(socket));
                gpaThread.start();

            }

        } catch (IOException ex) {
            Logger.getLogger(ThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static String calculateGPA(String clientMessage) {

        String[] message = clientMessage.split(", ");
        int numGrades = Character.getNumericValue(message[0].charAt(0));
        int i;
        int semesterCredits = 0;
        double semesterTotal = 0;

        for (i = 1; i < numGrades * 2; i += 2) {
            semesterCredits += Integer.parseInt(message[i + 1]);
            semesterTotal += (GPA_TABLE.get(message[i]) * Integer.parseInt(message[i + 1]));
        }

        DecimalFormat df = new DecimalFormat("#.##");
        String semesterGPA = df.format(semesterTotal / semesterCredits);

        double prevGPA = Double.parseDouble(message[i++]);
        int prevCreditHrs = Integer.parseInt(message[i]);
        double cumulativeGPANum = ((prevGPA * prevCreditHrs) + semesterTotal) / (prevCreditHrs += semesterCredits);
        String cumulativeGPA = df.format(cumulativeGPANum);

        return semesterGPA + ", " + cumulativeGPA + ", " + String.valueOf(prevCreditHrs);
    }

    static void initTable() {
        GPA_TABLE.put("A", 4.0);
        GPA_TABLE.put("A-", 3.7);
        GPA_TABLE.put("B+", 3.3);
        GPA_TABLE.put("B", 3.0);
        GPA_TABLE.put("B-", 2.7);
        GPA_TABLE.put("C+", 2.3);
        GPA_TABLE.put("C", 2.0);
        GPA_TABLE.put("C-", 1.7);
        GPA_TABLE.put("D+", 1.3);
        GPA_TABLE.put("D", 1.0);
        GPA_TABLE.put("F", 0.0);
    }

    static boolean notFinished(String clientMessage) {
        return !clientMessage.trim().equals("Ok");
    }

    private class GPAThread implements Runnable {

        private Socket socket;
        private DataOutputStream toClient;
        private DataInputStream fromClient;

        public GPAThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                
                fromClient = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                toClient = new DataOutputStream(socket.getOutputStream());

                String clientMessage = fromClient.readUTF();

                System.out.println("Recieved message from client:  " + clientMessage);

                while (notFinished(clientMessage)) {

                    String reply = calculateGPA(clientMessage);
                    toClient.writeUTF(reply);
                    
                    clientMessage = fromClient.readUTF();
                    System.out.println("Recieved message from client:  " + clientMessage);
                }
                
            socket.close();
            toClient.close();
            fromClient.close();
            
            System.out.println("Socket and thread closed");
                
            } catch (IOException ex) {
                Logger.getLogger(ThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
