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

    static int size = 128;
    
    public static void main(String ARGS[]) {
        Receiver rex = new Receiver();
    }

    public Receiver() {
        System.out.println("Got to receiver.");
        try {
            DatagramSocket receiverSocket = new DatagramSocket(9876);
            DatagramSocket acknowledgementSocket = new DatagramSocket(9879);
            byte[] rcvData = new byte[size];
            byte[] ackData = new byte[size];
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
            receiverSocket.receive(rcvPkt); // should grab all the packet info and everything
            InetAddress IPAddress = rcvPkt.getAddress();
            int port = receiverSocket.getPort();
            DatagramPacket ackPkt;
            rcvData = rcvPkt.getData();
            int sequence = rcvData[0];
            boolean drop[] = new boolean[sequence];
            drop = setDrop(rcvData, drop);
            ackPkt = new DatagramPacket(rcvData, rcvData.length, IPAddress, 9879);
            acknowledgementSocket.send(ackPkt);
            int i = 0; // this value will track received packets

            do {
                receiverSocket.receive(rcvPkt);
                rcvData = rcvPkt.getData();
                int a = rcvData[0];
                if (drop[a] == false) {
                    ackData = rcvData;
                    ackPkt = new DatagramPacket(rcvData, rcvData.length, IPAddress, 9879);
                    acknowledgementSocket.send(ackPkt);
                    i++;
                } else {
                    drop[a] = false;
                }
            } while (i != sequence);

        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean[] setDrop(byte[] in_byte, boolean[] in_bool) {

        for (int i = 1; i < in_byte.length; i++) {
            int value = in_byte[i];
            if (value > 0) {
                in_bool[value] = true;
            }

        }

        return in_bool;
    }

    //public void 
}
