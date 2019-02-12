package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.NetworkClient;
import network.NetworkServer;

public class Main extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Main.primaryStage = primaryStage;
        initiateNetwork();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();

        // Store access to the controller for later user
        primaryStage.setUserData(controller);

        // Alternative to setUserData
        // primaryStage.getProperties().put("controller", controller);

        // Alternative to the code in Main::stop();
//        primaryStage.setOnHidden( windowEvent -> controller.onShutdown() );

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

    }

    @Override
    public void stop(){
        Controller controller = (gui.Controller)primaryStage.getUserData();
        controller.onShutdown();
    }

    private void initiateNetwork() {
        // When trying to "get" the singleton instances they
        // they'll be constructed if they haven't already.
        NetworkServer.get();
        NetworkClient.get();
    }

    public static void main(String[] args) { launch(args); }
}
