package network;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingDeque;

public class NetworkServer {
    public final int PORT = 9001;
    private final int MSG_SIZE = 1024;

    // In the Server we store both "WHO sent the msg and WHAT was the msg"
    private LinkedBlockingDeque<Tuple<SocketAddress, Object>> msgQueue = new LinkedBlockingDeque<>();

    private DatagramSocket socket;
    private static NetworkServer _singleton = new NetworkServer();

    private NetworkServer() {
        try {
            socket = new DatagramSocket(PORT);
            socket.setSoTimeout(100);
        } catch(Exception e){
            e.printStackTrace();
        }

        Thread t = new Thread(this::loop);
        t.setDaemon(true);
        t.start();
    }

    public static NetworkServer get(){
        return _singleton;
    }

    public Tuple<SocketAddress, Object> pollMessage(){
        return msgQueue.pollFirst();
    }

    public void sendMsgToClient(String msg, SocketAddress clientSocketAddress) {
        byte[] buffer = msg.getBytes();

        DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientSocketAddress);

        try { socket.send(response); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void loop() {
        while (true) {
            DatagramPacket clientRequest = new DatagramPacket(new byte[MSG_SIZE], MSG_SIZE);

            if (!receiveMsgFromAnyClient(clientRequest)) {
                continue;
            }

            Object msg = deserializeRequest(clientRequest);
            msgQueue.addLast(new Tuple(clientRequest.getSocketAddress(), msg));
        }
    }

    private Object deserializeRequest(DatagramPacket clientRequest){
        try {
            try (ByteArrayInputStream bin = new ByteArrayInputStream(clientRequest.getData())) {
                try (ObjectInputStream ois = new ObjectInputStream(bin)) {
                    return ois.readObject();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean receiveMsgFromAnyClient(DatagramPacket clientRequest){
        try {
            socket.receive(clientRequest);
            return true;
        } catch (SocketTimeoutException e) { // Ignore timeout
        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }
}

