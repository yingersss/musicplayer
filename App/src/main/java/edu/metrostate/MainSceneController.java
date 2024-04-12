package edu.metrostate;

import classes.*;
import javafx.application.Platform;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {
    private MediaPlayer currentPlayer;
    private static final String SONG_LIST_FILE = "songs.txt"; // The name of the file to store the song paths

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
            System.out.println("Selected song: " + selectedSong.getFilePath()); // Debug: Check selected file path
            if (currentPlayer != null) {
                MediaPlayer.Status status = currentPlayer.getStatus();
                System.out.println("Current player status: " + status); // Debug: Check current player status
                if (status == MediaPlayer.Status.PLAYING) {
                    currentPlayer.pause();
                    setButtonIcon(playButton, "play.png"); // Change to play icon
                } else {
                    currentPlayer.play();
                    setButtonIcon(playButton, "pause.png"); // Change to pause icon
                }
            } else {
                try {
                    Media media = new Media(new File(selectedSong.getFilePath()).toURI().toString());
                    currentPlayer = new MediaPlayer(media);
                    currentPlayer.play();
                    setButtonIcon(playButton, "pause.png"); // Change to pause icon
                    System.out.println("Playing new song: " + selectedSong.getFilePath()); // Debug: Confirm new song is playing
                } catch (Exception e) {
                    System.err.println("Error playing media: " + e.getMessage()); // Debug: Catch any errors
                    e.printStackTrace();
                }
            }
        }
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

    // method to set a button to show the correct icon (play or pause)
    private void setButtonIcon(Button button, String iconName) {
        Image img = new Image(getClass().getResourceAsStream("/images/" + iconName));
        ImageView iconView = new ImageView(img);
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(40); // set the size as appropriate for your UI
        iconView.setFitHeight(40); // set the size as appropriate for your UI
        button.setGraphic(iconView);
    }
    private void loadSongList() {
        Path path = Paths.get(SONG_LIST_FILE);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    File file = new File(line); // Ensure the line is just a path
                    if (file.exists() && file.getName().toLowerCase().endsWith(".mp3")) {
                        Media media = new Media(file.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);

                        mediaPlayer.setOnReady(() -> {
                            String title = (String) media.getMetadata().get("title");
                            String artist = (String) media.getMetadata().get("artist");
                            String album = (String) media.getMetadata().get("album");
                            Double duration = media.getDuration().toSeconds();

                            if (title == null || title.isEmpty()) {
                                title = file.getName().substring(0, file.getName().lastIndexOf('.'));
                            }

                            Song song = new Song(title, artist, album, duration, "Unknown Genre", line);
                            Platform.runLater(() -> {
                                songListView.getItems().add(song);
                            });
                            mediaPlayer.dispose();
                        });
                        mediaPlayer.play();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Song createSongFromMedia(MediaPlayer mediaPlayer, File file) {
        String title = (String) mediaPlayer.getMedia().getMetadata().get("title");
        String artist = (String) mediaPlayer.getMedia().getMetadata().get("artist");
        String album = (String) mediaPlayer.getMedia().getMetadata().get("album");
        Double duration = mediaPlayer.getMedia().getDuration().toSeconds();
        if (title == null || title.isEmpty()) title = file.getName();
        return new Song(title, artist, album, duration, "Unknown Genre", file.getAbsolutePath());
    }

    private void saveSongList() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SONG_LIST_FILE))) {
            for (Song song : songListView.getItems()) {
                // Write only the absolute file path
                writer.write(new File(song.getFilePath()).getAbsolutePath());
                writer.newLine();
                System.out.println("Captured path: " + new File(song.getFilePath()).getAbsolutePath());
                System.out.println("Saving path: " + new File(song.getFilePath()).getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // Call this method when application is closing
    @FXML
    void handleApplicationClose() {
        saveSongList();
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSongList();
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
                        String absolutePath = file.getAbsolutePath(); // Get absolute path directly
                        Media media = new Media(file.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);

                        mediaPlayer.setOnReady(() -> {
                            String title = (String) media.getMetadata().get("title");
                            String artist = (String) media.getMetadata().get("artist");
                            String album = (String) media.getMetadata().get("album");
                            Double duration = media.getDuration().toSeconds();
                            if (title == null || title.isEmpty()) {
                                title = file.getName();
                            }
                            Song song = new Song(title, artist, album, duration, "Unknown Genre", absolutePath);
                            Platform.runLater(() -> {
                                songListView.getItems().add(song);
                            });
                            mediaPlayer.dispose();
                        });
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
}
