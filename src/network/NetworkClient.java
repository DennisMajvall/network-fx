package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingDeque;

public class NetworkClient {
    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 9001;
    private final int MSG_SIZE = 512;

    private DatagramSocket socket;
    private volatile boolean isRunning;

    private LinkedBlockingDeque<String> msgQueue = new LinkedBlockingDeque<>();
    private static NetworkClient _singleton = new NetworkClient();

    private NetworkClient() {
        try {
            socket = new DatagramSocket(0);
            socket.connect(InetAddress.getByName(SERVER_IP), SERVER_PORT);
            socket.setSoTimeout(100);
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
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        try { socket.send(request); } catch (Exception e) {}
    }

    private void receiveMessageFromServer() {
        byte[] buffer = new byte[MSG_SIZE];
        DatagramPacket response = new DatagramPacket(buffer, buffer.length);

        try {
            socket.receive(response);
            msgQueue.addLast(new String(buffer, 0, response.getLength()));
        } catch (SocketTimeoutException ex) { // Do nothing
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loop() {
        isRunning = true;

        while (isRunning) {
            receiveMessageFromServer();
        }
    }
}
