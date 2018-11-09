package network;

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class NetworkServer {
    public final int PORT = 80;
    private final int SLEEP_MS = 100;
    private final int MSG_SIZE = 512;

    // In the Server we store both "WHO sent the msg and WHAT was the msg"
    private LinkedBlockingDeque<Tuple<Integer, String>> msgQueue = new LinkedBlockingDeque<>();
    private ArrayList<SocketAddress> clients = new ArrayList<>();

    private DatagramSocket socket;
    private volatile boolean isRunning;
    private static NetworkServer _singleton = new NetworkServer();

    private NetworkServer(){
        try {
            socket = new DatagramSocket(80);
            socket.setSoTimeout(SLEEP_MS);
        } catch(SocketException e){ System.out.println(e.getMessage()); }

        new Thread(this::loop).start();
    }

    public static NetworkServer get(){
        return _singleton;
    }

    public void stop(){
        isRunning = false;
    }

    public Tuple<Integer, String> pollMessage(){
        return msgQueue.pollFirst();
    }

    public void sendMsgToClient(String msg, DatagramPacket clientRequest) {
        byte[] buffer = msg.getBytes();

        InetAddress clientAddress = clientRequest.getAddress();
        int clientPort = clientRequest.getPort();

        DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
        try { socket.send(response); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void loop() {
        isRunning = true;

        while (isRunning) {
            DatagramPacket clientRequest = new DatagramPacket(new byte[MSG_SIZE], MSG_SIZE);

            if (!receiveMessageFromAnyClient(clientRequest)) {
                continue;
            }

            String clientMsg = new String(clientRequest.getData(), 0, clientRequest.getLength());

            addNewClientIfNeeded(clientRequest);
            msgQueue.addLast(new Tuple(getClientIndex(clientRequest), clientMsg));

            sendMsgToClient("Hejsan", clientRequest);
        }
    }

    private boolean receiveMessageFromAnyClient(DatagramPacket clientRequest){
        try { socket.receive(clientRequest); }
        catch (Exception ex) {
            try { Thread.sleep(SLEEP_MS); }
            catch (InterruptedException e) { e.printStackTrace(); }
            return false;
        }
        return true;
    }

    private int getClientIndex(DatagramPacket client){
        return clients.indexOf((client.getSocketAddress()));
    }

    private void addNewClientIfNeeded(DatagramPacket client){
        if (!clients.contains(client.getSocketAddress())) {
            clients.add(client.getSocketAddress());
        }
    }
}

