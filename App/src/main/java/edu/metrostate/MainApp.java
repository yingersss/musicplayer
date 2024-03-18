package edu.metrostate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    private final ValueStore store;

    public MainApp() {
        this.store = new ValueStore();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // loading the fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("musicplayertest.fxml"));
        AnchorPane root = loader.load();

        // MainSceneController mainSceneController = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Music Player");
        stage.setResizable(false); // couldnt figure out how to dynamically stretch the album image or vbox that held the image
        //loadStylesheetIntoScene(scene);
        stage.show();

    }

    /*
    private void loadStylesheetIntoScene(Scene scene) {
        URL stylesheetURL = getClass().getResource("style.css");
        if (stylesheetURL == null) {
            return;
        }
        String urlString = stylesheetURL.toExternalForm();
        if (urlString == null) {
            return;
        }
        scene.getStylesheets().add(urlString);
    }
     */
}