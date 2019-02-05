package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingDeque;

public class NetworkClient {
    private final String SERVER_IP = "localhost";
    private final int MSG_SIZE = 512;
    private final int SLEEP_MS = 100;

    private DatagramSocket socket;
    private volatile boolean isRunning;

    private InetAddress serverAddress;
    private LinkedBlockingDeque<String> msgQueue = new LinkedBlockingDeque<>();
    private static NetworkClient _singleton = new NetworkClient();

    private NetworkClient(){
        try {
            serverAddress = InetAddress.getByName(SERVER_IP);

            socket = new DatagramSocket(0, serverAddress);
            socket.setSoTimeout(SLEEP_MS);
        } catch(Exception e){ System.out.println(e.getMessage()); }

        new Thread(this::loop).start();
    }

    public static NetworkClient get(){
        return _singleton;
    }

    public void stop(){
        isRunning = false;
    }

    public String pollMessage(){
        return msgQueue.pollFirst();
    }

    public void sendMsgToServer(String msg) {
        byte[] buffer = msg.getBytes();
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, this.serverAddress, NetworkServer.get().PORT);
        try { socket.send(request); } catch (Exception e) {}
    }

    private void receiveMessageFromServer() {
        byte[] buffer = new byte[MSG_SIZE];
        DatagramPacket response = new DatagramPacket(buffer, buffer.length);

        try {
            socket.receive(response);
            msgQueue.addLast(new String(buffer, 0, response.getLength()));
        } catch (Exception ex) {
            try { Thread.sleep(SLEEP_MS); }
            catch (Exception e) {}
        }
    }

    private void loop() {
        isRunning = true;

        while (isRunning) {
            receiveMessageFromServer();
        }
    }
}