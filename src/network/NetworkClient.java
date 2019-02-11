package network;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingDeque;

public class NetworkClient {
    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 9001;
    private final int MSG_SIZE = 1024;

    private DatagramSocket socket;

    private LinkedBlockingDeque<String> msgQueue = new LinkedBlockingDeque<>();
    private static NetworkClient _singleton = new NetworkClient();

    private NetworkClient() {
        try {
            socket = new DatagramSocket(0);
            socket.connect(InetAddress.getByName(SERVER_IP), SERVER_PORT);
            socket.setSoTimeout(100);
        } catch(Exception e){
            e.printStackTrace();
        }

        Thread t = new Thread(this::loop);
        t.setDaemon(true);
        t.start();
    }

    public static NetworkClient get(){
        return _singleton;
    }

    public String pollMessage(){
        return msgQueue.pollFirst();
    }

    // Delete this old method after presentation.
//    public void sendMsgToServer(String msg) {
//        byte[] buffer = msg.getBytes();
//        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
//        try { socket.send(request); } catch (Exception e) {}
//    }

    public void sendObjectToServer(Serializable object) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteArrayStream)) {
            out.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatagramPacket request = new DatagramPacket(byteArrayStream.toByteArray(), byteArrayStream.size());
        try { socket.send(request); }
        catch (Exception e) { e.printStackTrace();}
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
        while (true) {
            receiveMessageFromServer();
        }
    }
}
