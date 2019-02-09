package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;

public class NetworkServer {
    public final int PORT = 9001;
    private final int MSG_SIZE = 512;

    // In the Server we store both "WHO sent the msg and WHAT was the msg"
    private LinkedBlockingDeque<Tuple<SocketAddress, String>> msgQueue = new LinkedBlockingDeque<>();

    private DatagramSocket socket;
    private volatile boolean isRunning;
    private static NetworkServer _singleton = new NetworkServer();

    private NetworkServer() {
        try {
            socket = new DatagramSocket(PORT);
            socket.setSoTimeout(100);
        } catch(Exception e){ e.printStackTrace(); }

        new Thread(this::loop).start();
    }

    public static NetworkServer get(){
        return _singleton;
    }

    public void stop(){
        isRunning = false;
    }

    public Tuple<SocketAddress, String> pollMessage(){
        return msgQueue.pollFirst();
    }

    public void sendMsgToClient(String msg, SocketAddress clientSocketAddress) {
        byte[] buffer = msg.getBytes();

        DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientSocketAddress);

        try { socket.send(response); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void loop() {
        isRunning = true;

        while (isRunning) {
            DatagramPacket clientRequest = new DatagramPacket(new byte[MSG_SIZE], MSG_SIZE);

            if (!receiveMsgFromAnyClient(clientRequest)) {
                continue;
            }

            String clientMsg = new String(clientRequest.getData(), 0, clientRequest.getLength());
            msgQueue.addLast(new Tuple(clientRequest.getSocketAddress(), clientMsg));
        }
    }

    private boolean receiveMsgFromAnyClient(DatagramPacket clientRequest){
        try { socket.receive(clientRequest); }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
}

