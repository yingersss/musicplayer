package edu.metrostate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // loading the fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("musicplayertest.fxml"));
        AnchorPane root = loader.load();

        // Get the controller from the FXMLLoader
        MainSceneController mainSceneController = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Music Player");
        stage.setResizable(false); // Could not figure out how to dynamically stretch the album image or vbox that held the image
        stage.show();
        // Set the close request event to call the handleApplicationClose method
        // basically saving the songs list to a txtpad
        stage.setOnCloseRequest(event -> mainSceneController.handleApplicationClose());
    }
    public static void main(String[] args) {
        launch(args);
    }
}
