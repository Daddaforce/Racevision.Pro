package seng302.data;

import seng302.utilities.ConnectionUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Sends data out on the client side
 */
public class ClientSender {

    private Socket connection;

    public ClientSender(Socket socket) {
        this.connection = socket;
    }

    public void sendToServer(byte[] packet) {
        sendPacket(packet, connection);
    }

    /**
     * sends a packet to a given socket
     * @param packet the packet to send
     * @param socket the socket to send the packet on
     */
    private void sendPacket(byte[] packet, Socket socket){
        try {
            DataOutputStream clientOutput = new DataOutputStream(socket.getOutputStream());
            clientOutput.write(packet);
        } catch (SocketException e) {
            System.out.println("Client: Unable to reach game server");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            System.out.println("Client: Unable to reach VM server");
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
