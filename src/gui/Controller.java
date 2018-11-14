package gui;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;

import network.NetworkClient;
import network.NetworkServer;
import java.net.SocketAddress;

public class Controller {
    SocketAddress lastIncomingPlayer;

    @FXML
    public void initialize() {
        PauseTransition wait = new PauseTransition(javafx.util.Duration.millis(250));
        wait.setOnFinished((e) -> {
            checkServerIncomingMessages();
            checkClientIncomingMessages();

            wait.playFromStart();
        });
        wait.play();

        // Don't forget to stop(); when unloading the controller to prevent memory leaks.
    }

    public void sendMsgToServer(){
        NetworkClient.get().sendMsgToServer("Sup server?");
    }

    private void checkServerIncomingMessages(){
        var srvMsg = NetworkServer.get().pollMessage();
        if (srvMsg != null) {
            lastIncomingPlayer = srvMsg.left;
            System.out.println("Server got: " + srvMsg.right);
        }
    }

    private void checkClientIncomingMessages(){
        var srvMsg = NetworkClient.get().pollMessage();
        if (srvMsg != null) {
            System.out.println("Client got: " + srvMsg);
        }
    }

    public void sendMsgToClient(){
        NetworkServer.get().sendMsgToClient("Hello there Mr.Client", lastIncomingPlayer);
    }
}
