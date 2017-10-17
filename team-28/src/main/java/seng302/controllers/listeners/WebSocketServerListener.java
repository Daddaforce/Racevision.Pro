package seng302.controllers.listeners;

import seng302.data.AC35StreamMessage;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seng302.data.AC35StreamField.*;

/**
 * Server listener to connect and communicate with a web socket client.
 * A lot of the code is "based on" https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
 */
public class WebSocketServerListener extends AbstractServerListener {

    private BufferedInputStream socketData;

    WebSocketServerListener(Socket socket, BufferedInputStream socketData) throws IOException {
        setSocket(socket);
        this.socketData = socketData;
        sendWebSocketResponse();
    }

    /**
     * Currently only prints out what is received for testing, need to parse the packet in the future.
     */
    @Override
    public void run() {
        while(clientConnected){
            try {
                byte[] packet = readPacket();
                byte[] header = extractHeader(packet);

                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                int sourceId = byteArrayRangeToInt(header, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());

                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);

                byte[] body = extractBody(packet, messageLength);
                byte[] crc = extractCRC(packet);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                            break;
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body);
                            }
                        case REQUEST_AVAILABLE_RACES:
                            parseRequestRacesMessage(body);
                            break;
                    }
                } else{
                    System.out.println("Incorrect CRC");
                }
            } catch (IndexOutOfBoundsException e) {
                clientConnected = false;
                System.out.println("WebSocket has disconnected itself.");
            } catch (IOException e) {
                clientConnected = false;
                e.printStackTrace();
            }
        }
        System.out.println("Web Socket Server Listener Terminated");
    }

    private void parseRequestRacesMessage(byte[] body) {
        Integer roomCode = byteArrayRangeToInt(body, PARTY_MODE_ROOM_CODE.getStartIndex(), PARTY_MODE_ROOM_CODE.getEndIndex());
        setChanged();
        notifyObservers(roomCode);
    }

    private byte[] extractHeader(byte[] packet) {
        byte[] header = new byte[HEADER_LENGTH];
        System.arraycopy(packet, 0, header, 0, HEADER_LENGTH);
        return header;
    }

    private byte[] extractBody(byte[] packet, int bodyLength) {
        byte[] body = new byte[bodyLength];
        System.arraycopy(packet, HEADER_LENGTH, body, 0, bodyLength);
        return body;
    }

    private byte[] extractCRC(byte[] packet) {
        byte[] crc = new byte[CRC_LENGTH];
        int crcStartIndex = packet.length - CRC_LENGTH;
        System.arraycopy(packet, crcStartIndex, crc, 0, CRC_LENGTH);
        return crc;
    }

    /**
     * Sends the required WebSocket HTTP response to establish the handshake.
     * Code sourced from https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
     */
    private void sendWebSocketResponse() {
        try{
            String data = new Scanner(socketData, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
            Matcher get = Pattern.compile("^GET").matcher(data);
            System.out.println("Server: Accepted websocket Connection");
            if (get.find()) {
                byte[] response = generateResponseText(data);
                socket.getOutputStream().write(response, 0, response.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the HTTP response sent to establish the handshake.
     * @param data The data scanned from the socket scanner
     * @return The response in a byte array
     */
    private byte[] generateResponseText(String data) {
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
        match.find();
        byte[] response = null;
        try{
            response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + DatatypeConverter
                    .printBase64Binary(
                            MessageDigest.getInstance("SHA-1")
                                    .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                            .getBytes("UTF-8")))
                    + "\r\n\r\n")
                    .getBytes("UTF-8");
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Reads a whole packet in, parsing the WebSocket header and decoding the bytes in the process.
     * Assumes the first byte read is the first byte of the packet. (Is not out of sync).
     * @return The payload of the packet
     * @throws IOException If there is an issue with the connection/reading in data
     */
    private byte[] readPacket() throws IOException{
        byte[] key = new byte[4];
        int opcode = socketData.read();
        if(opcode == -1){
            throw new IOException("Bad WebSocket Frame Opcode: " + opcode);
        }
        int length = readPacketLength();
        for(int i = 0; i < 4; i++){
            key[i] = (byte)socketData.read();
        }
        byte[] encodedPacket = new byte[length];
        for(int i = 0; i < length; i++){
            encodedPacket[i] = (byte)socketData.read();
        }
        return decodePacket(encodedPacket, key);
    }

    /**
     * Parses the packet length as it is reading it in from the socket. Dealing with multiple cases
     * as part of the WebSocket protocol.
     * Assumes length will fit in a 32 bit signed integer (Integer.MAX_VALUE), otherwise
     * we'll be creating a byte array far too large anyways
     * @return The length of the packet.
     * @throws IOException If there is an issue with the connection/reading in data
     */
    private int readPacketLength() throws IOException {
        int length = socketData.read() - 128;
        if(length < 125) {
            return length;
        } else {
            int fieldLength = length == 126 ? 2 : 8;
            byte[] packetLength = new byte[fieldLength];
            for (int i = 0; i < fieldLength; i++) {
                packetLength[i] = (byte) socketData.read();
            }
            long lengthValue = byteArrayRangeToLong(packetLength, 0, fieldLength);
            if (lengthValue > Integer.MAX_VALUE){
                //Safety check, we should not receive a packet length this large
                throw new IllegalArgumentException("Packet length is too large");
            } else{
                return (int) lengthValue;
            }
        }
    }

    /**
     * Decodes the packet payload read in using the key given.
     * @param encodedPacket The packet payload with encoded bytes
     * @param key Array with 4 bytes for the 4 possible key values
     * @return A new byte array with the decoded payload
     * @throws IOException If there is an issue with the connection/reading in data
     */
    private byte[] decodePacket(byte[] encodedPacket, byte[] key)  {
        byte[] decodedPacket = new byte[encodedPacket.length];
        int keyIndex = 0;
        for(int i = 0; i < encodedPacket.length; i++) {
            decodedPacket[i] = (byte) (encodedPacket[i] ^ key[keyIndex & 0x3]);
            keyIndex++;
        }
        return decodedPacket;
    }
}
