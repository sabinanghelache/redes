package es.udc.redes.webserver;


import java.io.IOException;
import java.net.*;

public class WebServer {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Format: es.udc.redes.webserver.WebServer 5000");
            System.exit(-1);
        }

        Socket socket = null;

        try (ServerSocket sSocket = new ServerSocket(Integer.parseInt(args[0]))) {

            sSocket.setSoTimeout(300000);

            while (true) {

                socket = sSocket.accept();

                ServerThread sThread= new ServerThread(socket);

                sThread.start();
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //Close the socket
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
