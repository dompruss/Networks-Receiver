/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selectivereceiver;

/**
 *
 * @author Nik
 */
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {

    int window;
    static int size = 128;

    public static void main(String ARGS[]) {
        Receiver rec = new Receiver();
    }

    public Receiver() {
        try {

            DatagramSocket receiverSocket = new DatagramSocket(9876);
            DatagramSocket acknowledgementSocket = new DatagramSocket(9879);
            byte[] rcvData = new byte[size];
            byte[] ackData = new byte[size];
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
            receiverSocket.receive(rcvPkt); // should grab all the packet info and everything
            System.out.println("Received packet");
            InetAddress IPAddress = rcvPkt.getAddress();
            int port = receiverSocket.getPort();
            DatagramPacket ackPkt;
            rcvData = rcvPkt.getData();
            int sequence = rcvData[0];
            window = rcvData[1];
            boolean drop[] = new boolean[sequence];
            drop = setDrop(rcvData, drop);
            ackPkt = new DatagramPacket(rcvData, rcvData.length, IPAddress, 9878);
            acknowledgementSocket.send(ackPkt);
            int i = 0; // this value will track received packets
            boolean[] windowTracker = new boolean[sequence];
            for (int j = 0; j < sequence; j++) {
                windowTracker[j] = false;
            }

            int windowFirst = 0;
            do {
                receiverSocket.receive(rcvPkt);
                rcvData = rcvPkt.getData();
                int a = rcvData[0];
                if (drop[a] == false && windowTracker[a] != true) {
                    ackData[0] = (byte) a;
                    windowTracker[a] = true;
                    
                    System.out.println("Packet " + a + " is received, send Ack" + a + ", window" + windowMaker(windowFirst, windowTracker));

                    ackPkt = new DatagramPacket(ackData, ackData.length, IPAddress, 9878);
                    acknowledgementSocket.send(ackPkt);
                    i++;

                } else if(windowTracker[a] == true){
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
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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

    public boolean[] setDrop(byte[] in_byte, boolean[] in_bool) {

        for (int i = 2; i < size; i++) {
            int value = in_byte[i];
            if (value > 0) {
                in_bool[value] = true;
            }
        }
        return in_bool;
    }
    //public void 
}
