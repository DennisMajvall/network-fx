package gui;

import javafx.fxml.FXML;

import network.NetworkClient;
import network.NetworkServer;
import network.Person;

import java.net.SocketAddress;

public class Controller {
    SocketAddress lastIncomingPlayer;
    Thread myListeningThread;

    @FXML
    public void initialize() {
        myListeningThread = new Thread(()->{
            while(true) {
                checkServerIncomingMessages();
                checkClientIncomingMessages();

                // Without this 1 CPU core will constantly be at 100%
                try { Thread.sleep(1); }
                catch (Exception e) { break; }
            }
        });
        myListeningThread.start();


        // Test the de/serialization
        NetworkClient.get().sendObjectToServer(new Person());
        NetworkClient.get().sendObjectToServer("Hej");
    }

    // Called from Main.java
    public void onShutdown(){
        myListeningThread.interrupt();
    }

    public void sendMsgToServer(){
        NetworkClient.get().sendObjectToServer("Sup server?");
    }

    private void checkServerIncomingMessages(){
        var srvMsg = NetworkServer.get().pollMessage();
        if (srvMsg != null) {
            lastIncomingPlayer = srvMsg.left;
            if (srvMsg.right instanceof Person) {
                Person p = (Person)srvMsg.right;
                System.out.println("Server recieved a Person with name: " + p.getName() + " and age: " + p.getAge());
            } else {
                String s = (String)srvMsg.right;
                System.out.println("Server recieved the String: " + s);
            }
        }
    }

    private void checkClientIncomingMessages(){
        var clientMsg = NetworkClient.get().pollMessage();
        if (clientMsg  != null) {
            if (clientMsg instanceof String) {
                System.out.println("Client recieved the String: " + (String) clientMsg);
            } else {
                System.out.println("Client recieved something: " + clientMsg.toString());
            }
        }
    }

    public void sendMsgToClient(){
        if (lastIncomingPlayer != null) {
            NetworkServer.get().sendObjectToClient("Hello there Mr.Client", lastIncomingPlayer);
        } else {
            System.out.println("No client connected yet");
        }
    }
}
