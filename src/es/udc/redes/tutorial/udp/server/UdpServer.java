package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements a UDP echo server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: java es.udc.redes.tutorial.udp.server.UdpServer 5000");
            System.exit(-1);
        }
        try (DatagramSocket sDatagram = new DatagramSocket(Integer.parseInt(argv[0]))) {
            // Create a server socket

            // Set maximum timeout to 300 secs
            sDatagram.setSoTimeout(3000000);
            while (true) {
                // Prepare datagram for reception
                byte [] array  =  new byte[1024];
                DatagramPacket dgramRec = new DatagramPacket(array, array.length);

                // Receive the message
                sDatagram.receive(dgramRec);
                String message = new String(dgramRec.getData(), 0, dgramRec.getLength());
                System.out.println("SERVER: Received " + message + " from " + dgramRec.getAddress().toString() + ":" + dgramRec.getPort());

                // Prepare datagram to send response
                DatagramPacket dgramSent = new DatagramPacket(message.getBytes(),
                        message.getBytes().length, dgramRec.getAddress(), dgramRec.getPort());

                // Send response
                sDatagram.send(dgramSent);
                System.out.println("SERVER: Sending "
                        + new String(dgramSent.getData()) + " to "
                        + dgramSent.getAddress().toString() + ":"
                        + dgramSent.getPort());
            }

            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            //    System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the socket
        }
    }
}
