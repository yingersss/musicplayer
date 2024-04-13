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
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {
    private MediaPlayer currentPlayer;
    private Song currentSong;
    private static final String SONG_LIST_FILE = "songs.txt"; // The name of the file to store the song paths

    // left side column song listview
    @FXML
    private ListView<Song> songListView;
    // album image view
    @FXML
    private ImageView albumImageView;
    // progress bar on bottom of application
    @FXML
    private ProgressBar progressBar;
    // time label
    @FXML
    private Label timeLabel;
    @FXML
    private Slider volumeSlider;
    private float volume;
    public enum RepeatMode {
        NO_REPEAT, REPEAT_LIST, REPEAT_SONG
    }

    private RepeatMode repeatMode = RepeatMode.NO_REPEAT;


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
        System.out.println("Play button clicked.");
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();

        if (selectedSong == null) return; // No song selected, do nothing.

        if (currentPlayer != null) {
            MediaPlayer.Status status = currentPlayer.getStatus();
            if (currentSong != null && selectedSong.equals(currentSong)) {
                // If the selected song is the same as the current song
                if (status == MediaPlayer.Status.PLAYING) {
                    currentPlayer.pause(); // If it's playing, pause it.
                    setButtonIcon(playButton, "play.png");
                    return; // Do not proceed further, early exit.
                } else if (status == MediaPlayer.Status.PAUSED) {
                    currentPlayer.play(); // If it's paused, resume play.
                    setButtonIcon(playButton, "pause.png");
                    return; // Do not proceed further, early exit.
                }
                // No need to do anything if status is STOPPED, as a new MediaPlayer will be created.
            } else {
                // New song selected, stop and dispose of the current player.
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
                currentSong = null; // Clear current song as it's different.
            }
        }
        // At this point, either no MediaPlayer was present or a new song was selected.
        currentSong = selectedSong; // Update the current song.
        Media media = new Media(new File(selectedSong.getFilePath()).toURI().toString());
        currentPlayer = new MediaPlayer(media);
        setupMediaPlayerEvents();
        setupProgressBar(); // Call this method when MediaPlayer is ready and playing
        currentPlayer.play();
    }

    @FXML
    private void handlePreviousAction() {
        System.out.println("Previous button clicked.");
        if (songListView.getItems().isEmpty()) {
            // no songs in the list, do nothing
            return;
        }

        // checks if more than 3 seconds have been played
        if (currentPlayer != null && currentPlayer.getCurrentTime().greaterThan(javafx.util.Duration.seconds(3))) {
            System.out.println("Rewinding current song.");
            // Rewind the current song
            currentPlayer.seek(javafx.util.Duration.ZERO);
        } else {
            // otherwise, select the previous song
            int currentIndex = songListView.getSelectionModel().getSelectedIndex();
            int previousIndex = currentIndex - 1;
            if (previousIndex < 0) {
                // if the current index is the first song, wrap around to the last song in the list
                previousIndex = songListView.getItems().size() - 1;
            }
            songListView.getSelectionModel().select(previousIndex);

            // plays the newly selected song
            playSelectedSong();
        }
    }

    @FXML
    private void handleNextAction() {
        if (songListView.getItems().isEmpty()) {
            return;
        }
        int currentIndex = songListView.getSelectionModel().getSelectedIndex();
        int nextIndex = currentIndex + 1;

        if (nextIndex >= songListView.getItems().size()) { // At end of the list
            switch (repeatMode) {
                case REPEAT_LIST:
                    nextIndex = 0;  // Wrap to the start of the list
                    break;
                case REPEAT_SONG:
                    nextIndex = currentIndex;  // Stay on the current song
                    break;
                default:
                    return;  // No repeat, do nothing further
            }
        }

        songListView.getSelectionModel().select(nextIndex);
        playSelectedSong();
    }

    @FXML
    private void handleShuffleAction() {
    }
    @FXML
    private void handleRepeatAction() {
        // Cycle through the repeat modes
        switch (repeatMode) {
            case NO_REPEAT:
                repeatMode = RepeatMode.REPEAT_LIST;
                System.out.println("Repeat mode set to: REPEAT_LIST");
                break;
            case REPEAT_LIST:
                repeatMode = RepeatMode.REPEAT_SONG;
                System.out.println("Repeat mode set to: REPEAT_SONG");
                break;
            case REPEAT_SONG:
                repeatMode = RepeatMode.NO_REPEAT;
                System.out.println("Repeat mode set to: NO_REPEAT");
                break;
        }
        updateRepeatButtonIcon();  // Optionally update the button icon or text based on the current mode
    }

    private void updateRepeatButtonIcon() {
        if (repeatButton == null) {
            System.out.println("Repeat button is not initialized!");
            return;
        }

        switch (repeatMode) {
            case NO_REPEAT:
                setButtonIcon(repeatButton, "repeat.png");
                break;
            case REPEAT_LIST:
                setButtonIcon(repeatButton, "repeat_2.png");
                break;
            case REPEAT_SONG:
                setButtonIcon(repeatButton, "repeat_3.png");
                break;
        }
    }
    private void initializeProgressBar() {
        progressBar.setProgress(0); // Set the progress to 0
    }

    private void playSelectedSong() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) return; // No song selected, do nothing.

        if (currentPlayer != null) { // disposes the currentPlayer
            currentPlayer.stop();
            currentPlayer.dispose();
        }
        System.out.println("Creating MediaPlayer for: " + selectedSong.getFilePath());
        currentSong = selectedSong; // Update the current song
        System.out.println(selectedSong.getFilePath());
        Media media = new Media(new File(selectedSong.getFilePath()).toURI().toString());
        currentPlayer = new MediaPlayer(media);
        setupMediaPlayerEvents();
        setupProgressBar();
        currentPlayer.play();
    }
    private void setupProgressBar() {
        if (currentPlayer != null) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            currentPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                javafx.util.Duration currentTime = newValue;
                javafx.util.Duration totalDuration = currentPlayer.getTotalDuration();

                if (currentPlayer != null && totalDuration != null) {
                    progressBar.setProgress(currentTime.toMillis() / totalDuration.toMillis());
                    updateTimeLabel(currentTime, totalDuration);
                }
            });
        }
    }
    private void updateTimeLabel(javafx.util.Duration currentTime, javafx.util.Duration totalDuration) {
        int currentSeconds = (int) currentTime.toSeconds();
        int totalSeconds = (int) totalDuration.toSeconds();
        String timeText = String.format("%d:%02d / %d:%02d",
                currentSeconds / 60, currentSeconds % 60,
                totalSeconds / 60, totalSeconds % 60);
        timeLabel.setText(timeText);
    }
    private void setupMediaPlayerEvents() {
        if (currentPlayer != null) {
            currentPlayer.setOnError(() -> {
                System.out.println("Error with: " + currentSong.getFilePath());
                System.out.println(currentPlayer.getError().getMessage());
            });

            currentPlayer.setOnPlaying(() -> {
                System.out.println("Playing: " + currentSong.getFilePath());
                setButtonIcon(playButton, "pause.png");
            });

            currentPlayer.setOnPaused(() -> {
                System.out.println("MediaPlayer is paused.");
                setButtonIcon(playButton, "play.png");
            });
            /*
            currentPlayer.setOnStopped(() -> {
                System.out.println("Stopped: " + currentSong.getFilePath());
                setButtonIcon(playButton, "play.png");
                progressBar.setProgress(0);
                cleanupMediaPlayer();
            });
             */
            currentPlayer.setOnEndOfMedia(() -> {
                System.out.println("End of media.");
                int currentIndex = songListView.getSelectionModel().getSelectedIndex();
                int nextIndex = currentIndex + 1;

                if (nextIndex >= songListView.getItems().size()) { // At end of the list
                    switch (repeatMode) {
                        case REPEAT_LIST:
                            nextIndex = 0;  // Wrap to the start of the list
                            break;
                        case REPEAT_SONG:
                            nextIndex = currentIndex;  // Stay on the current song
                            break;
                        default:
                            return;  // No repeat, do nothing further
                    }
                }

                songListView.getSelectionModel().select(nextIndex);
                playSelectedSong();
            });

            // This is where you set up the volume control when the MediaPlayer is ready.
            currentPlayer.setOnReady(() -> {
                setupVolumeControl();
                System.out.println("MediaPlayer is ready. Duration: " + currentPlayer.getMedia().getDuration().toSeconds() + " seconds");
                //currentPlayer.play();
            });
        }
    }

    private void cleanupMediaPlayer() {
        if (currentPlayer != null) {
            System.out.println("Disposing MediaPlayer for: " + currentSong.getFilePath());
            currentPlayer.currentTimeProperty().removeListener((obs, oldTime, newTime) -> {
                if (currentPlayer != null && currentPlayer.getTotalDuration() != null) {
                    progressBar.setProgress(newTime.toMillis() / currentPlayer.getTotalDuration().toMillis());
                }
            });
            currentPlayer.dispose();
            currentPlayer = null;
            currentSong = null; // Reset the current song as playback has finished.
        }
    }

    private void setupVolumeControl() {
        if (currentPlayer != null) {
            volumeSlider.setValue(currentPlayer.getVolume() * 100); // assuming your volumeSlider's max is 100
            currentPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100)); // if your volumeSlider's max is 1, just bind without division
        }
    }

    // method to set a button to show the correct icon (play or pause)
    private void setButtonIcon(Button button, String iconName) {
        Image img = new Image(getClass().getResourceAsStream("/images/" + iconName));
        ImageView iconView = new ImageView(img);
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(45);
        iconView.setFitHeight(45);
        button.setGraphic(iconView);
    }
    private void loadSongList() {
        Path path = Paths.get(SONG_LIST_FILE);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    File file = new File(line);
                    if (file.exists() && file.getName().toLowerCase().endsWith(".mp3")) {
                        Media media = new Media(file.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);

                        mediaPlayer.setOnReady(() -> {
                            String title = (String) media.getMetadata().get("title");
                            String artist = (String) media.getMetadata().get("artist");
                            String album = (String) media.getMetadata().get("album");
                            Double duration = media.getDuration().toSeconds();
                            Image albumImage = (Image) media.getMetadata().get("image"); // Extract the album art

                            if (title == null || title.isEmpty()) {
                                title = file.getName().substring(0, file.getName().lastIndexOf('.'));
                            }

                            Song song = new Song(title, artist, album, duration, "Unknown Genre", line);
                            song.setAlbumImage(albumImage); // Set the album art in the Song object

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

    private void updateSongInfoDisplay(Song song) {
        if (song != null) {
            songNameInfo.setText(song.getTrackTitle());
            artistNameInfo.setText(song.getTrackAuthor());
            albumNameInfo.setText(song.getAlbumName());
            genreInfo.setText(song.getGenre());

            Image albumArt = song.getAlbumImage();
            if (albumArt != null) {
                albumImageView.setImage(albumArt);
            } else {
                albumImageView.setImage(null); // Set to a default image or clear it if there's no album art
            }
        } else {
            // Clear the labels and the album image if there's no song selected
            songNameInfo.setText("");
            artistNameInfo.setText("");
            albumNameInfo.setText("");
            genreInfo.setText("");
            albumImageView.setImage(null);
        }
    }


    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSongList();
        initializeProgressBar();
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
                            Image albumImage = (Image) media.getMetadata().get("image"); // extracts album art
                            if (title == null || title.isEmpty()) {
                                title = file.getName();
                            }
                            Song song = new Song(title, artist, album, duration, "Unknown Genre", absolutePath);
                            song.setAlbumImage(albumImage); // Set the album art in the Song object
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

        // Add a ChangeListener to the ListView's selection model
        songListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateSongInfoDisplay(newValue);
        });
    }
}
