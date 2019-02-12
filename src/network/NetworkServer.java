package network;

import java.io.*;
import java.net.*;
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

    // Delete this old method after presentation.
//    public void sendMsgToClient(String msg, SocketAddress clientSocketAddress) {
//        byte[] buffer = msg.getBytes();
//
//        DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientSocketAddress);
//
//        try { socket.send(response); }
//        catch (Exception e) { e.printStackTrace(); }
//    }

    public void sendObjectToClient(Serializable object, SocketAddress clientSocketAddress) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayStream)) {
            out.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatagramPacket request = new DatagramPacket(byteArrayStream.toByteArray(), byteArrayStream.size(), clientSocketAddress);
        try { socket.send(request); }
        catch (Exception e) { e.printStackTrace();}
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

    private boolean receiveMsgFromAnyClient(DatagramPacket clientRequest){
        try {
            socket.receive(clientRequest);
            return true;
        } catch (SocketTimeoutException e) { // Ignore timeout
        } catch (Exception e) { e.printStackTrace(); }

        return false;
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
}

