package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.NetworkClient;
import network.NetworkServer;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        initiateNetwork();

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    @Override
    public void stop(){
        System.out.println("stopping threads");
        NetworkServer.get().stop();
        NetworkClient.get().stop();
    }

    private void initiateNetwork() {
        // When trying to "get" the singleton instances they
        // they'll be constructed if they haven't already.
        NetworkServer.get();
        NetworkClient.get();
    }

    public static void main(String[] args) { launch(args); }
}
