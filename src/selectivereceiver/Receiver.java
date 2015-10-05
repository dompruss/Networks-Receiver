/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selectivereceiver;

/**
 *
 * @author Dominik Pruss
 * @author Nicolas Moore
 */
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {

    int window;                     // Initialize window base
    static int size = 128;          // Size for incoming data

    // Main method
    public static void main(String ARGS[]) {
        Receiver rec = new Receiver();
    }
    
    // Constructor
    public Receiver() {
        try {

            DatagramSocket receiverSocket = new DatagramSocket(9876);               // Initialize receiving socket
            DatagramSocket acknowledgementSocket = new DatagramSocket(9879);        // Initialize ack socket
            byte[] rcvData = new byte[size];                                        // Initialize array for data
            byte[] ackData = new byte[size];                                        // Initialize array for acks
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);    // Initialize Receive packet
            receiverSocket.receive(rcvPkt);                                         // should grab all the packet info and everything
            System.out.println("Received packet");
            InetAddress IPAddress = rcvPkt.getAddress();                            // Get IP and port
            int port = receiverSocket.getPort();
            DatagramPacket ackPkt;                                                  // Set up ack packet
            rcvData = rcvPkt.getData();                                             // Receive data
            int sequence = rcvData[0];                                              // Get sequence num
            window = rcvData[1];                                                    // Get window base
            boolean drop[] = new boolean[sequence];                                 // Find which packets are dropped
            drop = setDrop(rcvData, drop);
            ackPkt = new DatagramPacket(rcvData, rcvData.length, IPAddress, 9879);  // Set up ack packet
            acknowledgementSocket.send(ackPkt);                                     // Send acks
            int i = 0;                                                              // this value will track received packets
            boolean[] windowTracker = new boolean[sequence];                        // Track packets
            for (int j = 0; j < sequence; j++) {
                windowTracker[j] = false;
            }

            int windowFirst = 0;                                                    // Window base
            do {
                receiverSocket.receive(rcvPkt);                                     // Receive packet     
                rcvData = rcvPkt.getData();                                         // Get data
                int a = rcvData[0];                                                 // Get sequence num
                if (drop[a] == false && windowTracker[a] != true) {                 // If not dropped print message and send ack then increment i
                    ackData[0] = (byte) a;
                    windowTracker[a] = true;
                    
                    System.out.println("Packet " + a + " is received, send Ack" + a + ", window" + windowMaker(windowFirst, windowTracker));

                    ackPkt = new DatagramPacket(ackData, ackData.length, IPAddress, 9878);
                    acknowledgementSocket.send(ackPkt);
                    i++;

                } else if(windowTracker[a] == true){                                // If packet already received print message
                    System.out.println("Receveied a second copy of "+a);
                } 
                else {
                    drop[a] = false;
                }
                while(windowTracker[windowFirst] == true && windowFirst != sequence-1){
                        windowFirst++;
                    
                }
            } while (i != sequence);

        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex); // Catch IOExceptions
        }
    }

    // Method to print window on receiver side
    public String windowMaker(int windowfirst, boolean[] windowTracker) {
        String message = "[";
        for (int i = 0; i < window; i++) {
            
            if (windowfirst + i < windowTracker.length) {
                if (windowTracker[windowfirst + i]) {
                    message = message + "" + (windowfirst + i) + "#";
                } else {
                    message = message + "" + (windowfirst + i) + "";
                }

            } else {
                message = message + "-";
            }

            if (i != window - 1) {
                message = message + ",";
            } else {
                message = message + "]";
            }
        }
            return message;
        
    }
    
    // Method to see which packets are being dropped
    public boolean[] setDrop(byte[] in_byte, boolean[] in_bool) {

        for (int i = 2; i < size; i++) {
            int value = in_byte[i];
            if (value > 0) {
                in_bool[value] = true;
            }
        }
        return in_bool;
    }
}
