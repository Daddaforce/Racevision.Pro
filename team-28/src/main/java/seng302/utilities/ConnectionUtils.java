package seng302.utilities;

import seng302.controllers.Client;
import seng302.controllers.GameClient;
import seng302.controllers.GameServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Objects;

public class ConnectionUtils {
    private static GameClient client;
    private static final String GAME_RECORDER_IP = "132.181.16.17";
    private static final int GAME_RECORDER_PORT = 2827;
    public static final int DEFAULT_GAME_PORT = 2828;
    private static GameServer server;
    private static final String IP_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";


    /**
     * checks a given IP to make sure it is valid
     * @param IP the IP to be checked
     * @return a boolean of whether it is valid or not
     */
    public static Boolean IPRegExMatcher(String IP){
        return(IP.matches(IP_REGEX) || IP.equals("localhost"));
    }

    /**
     * checks to determine whether the port number is a valid regex. Colors the port number accordingly.
     * @return whether the port is valid or not
     */
    public static boolean validatePort(int port){
        return port > 1024 && port < 65536;
    }

    public static Client getClient() {
        return client;
    }

    public static void setClient(GameClient client) {
        ConnectionUtils.client = client;
    }

    public static void setServer(GameServer server) {
        ConnectionUtils.server = server;
    }

    public static void initiateDisconnect(boolean isHost)  {
        client.initiateClientDisconnect();
        if(isHost){
            System.out.println("Client: Cancelling race");
            server.initiateServerDisconnect();
        }
    }

    /**
     * gets users public ip address from AWS ping servers.
     * @return the user's public IP address or null if there was issue connecting to AWS.
     */
    public static String getPublicIp() {
        try{
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            if (Objects.equals(ipAddress.split("\\.")[0], "127")) {
                URL ipURL = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        ipURL.openStream()));
                ipAddress = in.readLine(); //you get the IP as a String
            }
            if (ipAddress.matches(IP_REGEX)) {
                return ipAddress;
            } else {
                return InetAddress.getLocalHost().getHostAddress();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String ipLongToString(long longIp){
        String ip = "";
        for (int i = 0; i < 4; i++){
            int num = (int)(longIp / Math.pow(256, (3 - i)));
            longIp = longIp - (long)(num * Math.pow(256, (3 - i)));
            if (i == 0){
                ip = String.valueOf(num);
            } else {
                ip = ip + "." + String.valueOf(num);
            }
        }
        return ip;
    }

    /**
     * Converts an ip string to a long to be sent in a packet
     * @param ip ip string
     * @return long that corresponds to ip address
     */
    public static long ipStringToLong(String ip){
        String[] ipBytes;
        double num = 0;
        if (ip != null){
            ipBytes = ip.split("\\.");
            for (int i = ipBytes.length - 1; i >= 0; i--){
                num += ((Integer.parseInt(ipBytes[i]) % 256) * Math.pow(256, (3 - i)));
            }
        }
        return (long) num;
    }

    public static String getGameRecorderIP() {
        return GAME_RECORDER_IP;
    }

    public static int getGameRecorderPort() {return GAME_RECORDER_PORT;}
}
