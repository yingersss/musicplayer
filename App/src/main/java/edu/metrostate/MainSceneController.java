package edu.metrostate;

import classes.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {
    private MediaPlayer currentPlayer;


    @FXML
    private Label label;

    @FXML
    private Label value;

    // left side column song listview
    @FXML
    private ListView<Song> songListView;

    // this is right side song information column
    @FXML private Label songNameInfo;
    @FXML private Label artistNameInfo;
    @FXML private Label albumNameInfo;
    @FXML private Label genreInfo;
    // buttons
    @FXML private Button playButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button shuffleButton;
    @FXML private Button repeatButton;

    // action handlers
    @FXML
    private void handlePlayAction() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            if (currentPlayer != null) {
                MediaPlayer.Status status = currentPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    currentPlayer.pause();
                    setButtonIcon(playButton, "play.png"); // Change to play icon
                } else {
                    currentPlayer.play();
                    setButtonIcon(playButton, "pause.png"); // Change to pause icon
                }
            } else {
                // Create a new MediaPlayer to play the selected song
                Media media = new Media(selectedSong.getFilePath());
                currentPlayer = new MediaPlayer(media);
                currentPlayer.play();
                setButtonIcon(playButton, "pause.png"); // Change to pause icon
            }
        }
    }
    // method to set a button to show the correct icon (play or pause)
    private void setButtonIcon(Button button, String iconName) {
        Image img = new Image(getClass().getResourceAsStream("/images/" + iconName));
        ImageView iconView = new ImageView(img);
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(40); // set the size as appropriate for your UI
        iconView.setFitHeight(40); // set the size as appropriate for your UI
        button.setGraphic(iconView);
    }
    @FXML
    private void handlePreviousAction() {
    }
    @FXML
    private void handleNextAction() {
    }
    @FXML
    private void handleShuffleAction() {
    }
    @FXML
    private void handleRepeatAction() {
    }



    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Configure the ListView to accept dragged files
        songListView.setOnDragOver(event -> {
            if (event.getGestureSource() != songListView &&
                    event.getDragboard().hasFiles()) {
            /* The drag-and-drop gesture is accepted only if files are being dragged
               over the ListView and the source of the drag is not the ListView itself */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        songListView.setOnDragEntered(event -> {
            if (event.getGestureSource() != songListView &&
                    event.getDragboard().hasFiles()) {
                // Optional: Visual feedback that the ListView is ready to accept files
                songListView.setStyle("-fx-border-color: blue;");
            }
        });

        songListView.setOnDragExited(event -> {
            // Optional: Reset the visual feedback when the drag exits the ListView
            songListView.setStyle("");
        });

        songListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    if (file.getName().toLowerCase().endsWith(".mp3")) {
                        // Create a Media object for the dragged MP3 file
                        Media media = new Media(file.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);

                        mediaPlayer.setOnReady(() -> {
                            // Extract metadata once it's available
                            String title = (String) media.getMetadata().get("title");
                            String artist = (String) media.getMetadata().get("artist");
                            String album = (String) media.getMetadata().get("album");
                            Double duration = media.getDuration().toSeconds();

                            // Use a default title if no metadata title is found
                            if (title == null || title.isEmpty()) {
                                title = file.getName();
                            }

                            // Create a new Song object and add it to the ListView
                            Song song = new Song(title, artist, album, duration, "Unknown Genre");
                            song.setFilePath(file.toURI().toString()); // Make sure you have a setter for filePath in your Song class
                            songListView.getItems().add(song);

                            // Stop the MediaPlayer as we only needed it for loading metadata
                            mediaPlayer.stop();
                        });

                        // Play the MediaPlayer which will trigger loading of the media and metadata
                        mediaPlayer.play();
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        // creating observableList and populating
        ObservableList<Song> songObservableList = FXCollections.observableArrayList();

        // setting songListView with the items/songs in the songObservableList
        songListView.setItems(songObservableList);
        // cell factory so that it displays string title rather than object reference
        songListView.setCellFactory(param -> new ListCell<>() {
            @Override
            public void updateItem(Song song, boolean empty) {
                // call default implementation
                super.updateItem(song, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(song.getTrackTitle());
                }
            }
        });


    }

    // method to set a button to be round as well as to set images within them
    public void setButtonImage (Button button, String imgFilePath) {
        // button style
        String buttonStyle = "-fx-background-radius: 5em; " +
                "-fx-min-width: 40px; " +
                "-fx-min-height: 40px; " +
                "-fx-max-width: 40px; " +
                "-fx-max-height: 40px;";

        button.setPrefSize(50, 50);
        Image img = new Image(imgFilePath);
        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.fitHeightProperty().bind(button.heightProperty());
        view.fitWidthProperty().bind(button.widthProperty());
        button.setGraphic(view);
        button.setContentDisplay(ContentDisplay.CENTER);
        view.setTranslateX(2); // only if needed
        button.setStyle(buttonStyle);
    }
}
