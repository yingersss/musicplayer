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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainSceneController implements Initializable {
    private ObservableList<Song> masterSongList = FXCollections.observableArrayList();
    private ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private MediaPlayer currentPlayer;
    private Song currentSong;
    private static final String SONG_LIST_FILE = "songs.txt"; // The name of the file to store the song paths

    // left side column song listview
    @FXML
    private ListView<Song> songListView;
    @FXML
    private ListView<Playlist> playlistListView;
    // album image view
    @FXML
    private ImageView albumImageView;
    // progress bar on bottom of application
    @FXML
    private Slider progressSlider;
    // time label
    @FXML
    private Label timeLabel;
    @FXML
    private Slider volumeSlider;
    public enum RepeatMode {
        NO_REPEAT, REPEAT_LIST, REPEAT_SONG
    }
    private ObservableList<Song> songObservableList = FXCollections.observableArrayList();
    private ObservableList<Song> shuffledSongsObservableList;
    private boolean isShuffled = false;
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
    private void handleViewAllSongsAction() {
        // Reset the song list view to the master song list
        displayMasterLibrary();
    }

    @FXML
    private void handlePlayAction() {
        System.out.println("Play button clicked.");
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();

        if (selectedSong == null) {
            System.out.println("No song selected.");
            return; // No song selected, do nothing.
        }

        // Debug print the song selected
        System.out.println("Selected song: " + selectedSong.getTrackTitle());

        // Check if the selected song is different from the currently playing song.
        if (currentPlayer != null && !selectedSong.equals(currentSong)) {
            System.out.println("Changing from song: " + (currentSong != null ? currentSong.getTrackTitle() : "none") + " to " + selectedSong.getTrackTitle());
            // If a different song is selected, stop and dispose of the current player.
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
            createAndPlayMedia(selectedSong);
        } else if (currentPlayer == null) {
            System.out.println("Debug: MediaPlayer is null, creating new player.");
            createAndPlayMedia(selectedSong);
        } else {
            // If the same song is re-selected, toggle play/pause.
            togglePlayPause();
        }
    }

    private void createAndPlayMedia(Song song) {
        if (song == null) {
            System.out.println("Debug: Song is null and cannot play.");
            return;
        }

        System.out.println("Creating new MediaPlayer for: " + song.getFilePath());

        currentSong = song; // Ensure this is set before any operation that might need it

        if (currentPlayer != null) {
            System.out.println("Debug: Stopping and disposing current MediaPlayer.");
            currentPlayer.stop();
            currentPlayer.dispose();
        }

        System.out.println("Debug: Creating MediaPlayer for song: " + song.getFilePath());
        Media media = new Media(new File(song.getFilePath()).toURI().toString());
        currentPlayer = new MediaPlayer(media);
        // Set volume control when MediaPlayer is ready
        currentPlayer.setOnReady(this::setupVolumeControl);

        setupMediaPlayerEvents(); // Set up media player events
        setupProgressSlider();
        currentPlayer.play();
        System.out.println("Debug: MediaPlayer created and playing.");
        setButtonIcon(playButton, "pause.png");
    }

    private void togglePlayPause() {
        if (currentPlayer == null) {
            System.out.println("Debug: MediaPlayer is null in togglePlayPause.");
            return;
        }

        MediaPlayer.Status status = currentPlayer.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            System.out.println("Pausing: " + currentSong.getTrackTitle());
            currentPlayer.pause();
            setButtonIcon(playButton, "play.png");
        } else {
            System.out.println("Resuming: " + currentSong.getTrackTitle());
            currentPlayer.play();
            setButtonIcon(playButton, "pause.png");
        }
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
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        ObservableList<Song> currentList = selectedPlaylist != null ? selectedPlaylist.getSongs() : masterSongList;

        if (!isShuffled) {
            // Backup the current list
            shuffledSongsObservableList = FXCollections.observableArrayList(currentList);
            // Shuffle the copy
            FXCollections.shuffle(shuffledSongsObservableList);
            // Set the shuffled list as the items
            songListView.setItems(shuffledSongsObservableList);
            isShuffled = true;
            setButtonIcon(shuffleButton, "shuffle_2.png");
            System.out.println("Shuffle Mode On.");
        } else {
            // Revert back to the original list
            songListView.setItems(currentList);
            isShuffled = false;
            setButtonIcon(shuffleButton, "shuffle.png");
            System.out.println("Shuffle Mode Off.");
        }
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

    @FXML
    private void handleSliderMouseClicked(MouseEvent event) {
        // not currently being used
    }
    @FXML
    private void handleSliderMouseReleased(MouseEvent event) {
        if (currentPlayer != null && currentPlayer.getMedia() != null) {
            currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
        }
    }

    @FXML
    private void handleSliderDrag(MouseEvent event) {
        if (currentPlayer != null) {
            currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
        }
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
        setupProgressSlider();
        currentPlayer.play();
    }
    private void setupProgressSlider() {
        if (currentPlayer != null) {
            progressSlider.setValue(0); // Set the initial slider value to 0

            currentPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                javafx.util.Duration currentTime = newValue;
                javafx.util.Duration totalDuration = currentPlayer.getTotalDuration();

                if (currentPlayer != null && totalDuration != null) {
                    progressSlider.setValue(currentTime.toMillis() / totalDuration.toMillis() * progressSlider.getMax());
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
        if (currentPlayer == null) {
            System.out.println("Debug: MediaPlayer is null in setUpMediaPlayerEvents.");
            return;
        }

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

        currentPlayer.setOnEndOfMedia(() -> {
            System.out.println("End of media.");
            switch (repeatMode) {
                case REPEAT_LIST:
                    handleNextAction();
                    break;
                case REPEAT_SONG:
                    currentPlayer.seek(Duration.ZERO);
                    currentPlayer.play();
                    System.out.println("Repeating song: " + (currentSong != null ? currentSong.getFilePath() : "unknown path"));
                    break;
                default:
                    handleNextAction();
            }
        });

        currentPlayer.setOnReady(() -> {
            setupVolumeControl();
            progressSlider.setMax(currentPlayer.getTotalDuration().toSeconds());
        });

        currentPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                Platform.runLater(() -> {
                    progressSlider.setValue(newValue.toSeconds());
                });
            }
        });
    }

    private void setupVolumeControl() {
        if (currentPlayer != null) {
            // Bind the volume property of MediaPlayer to the value of the volume slider.
            // This will cause the volume of the MediaPlayer to be updated whenever the slider is moved.
            currentPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
            // Initialize the slider position to the current volume.
            volumeSlider.setValue(currentPlayer.getVolume() * 100);
        }
    }

    // method to set a button to show the correct icon (play or pause)
    private void setButtonIcon(Button button, String iconName) {
        Platform.runLater(() -> {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/" + iconName)));
            ImageView iconView = new ImageView(img);
            iconView.setPreserveRatio(true);
            iconView.setFitWidth(45);
            iconView.setFitHeight(45);
            button.setGraphic(iconView);
        });
    }
    private void loadMasterSongList() {
        Path path = Paths.get(SONG_LIST_FILE);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    File file = new File(line);
                    if (file.exists() && file.getName().toLowerCase().endsWith(".mp3")) {
                        createSongFromFile(file, song -> {
                            Platform.runLater(() -> {
                                masterSongList.add(song);
                                songListView.getItems().add(song);
                            });
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void createSongFromFile(File file, SongReadyCallback callback) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            String title = (String) media.getMetadata().get("title");
            String artist = (String) media.getMetadata().get("artist");
            String album = (String) media.getMetadata().get("album");
            double duration = media.getDuration().toSeconds();
            Image albumImage = (Image) media.getMetadata().get("image");

            if (title == null || title.isEmpty()) {
                title = file.getName().substring(0, file.getName().lastIndexOf('.'));
            }

            Song song = new Song(title, artist, album, duration, "Unknown Genre", file.getAbsolutePath());
            song.setAlbumImage(albumImage);
            mediaPlayer.dispose();

            if (callback != null) {
                callback.onSongReady(song);
            }
        });
        mediaPlayer.play(); // Again, consider if this is necessary
    }


    // Method to save all playlists to a file
    private void savePlaylists() {
        String playlistFileName = "playlists.txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(playlistFileName))) {
            for (Playlist playlist : playlists) {
                writer.write(playlist.getName());
                for (Song song : playlist.getSongs()) {
                    writer.write(";" + song.getFilePath()); // Delimit songs with semicolon
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaylists() {
        String playlistFileName = "playlists.txt";
        Path playlistFilePath = Paths.get(playlistFileName);

        // Check if the playlist file exists
        if (Files.exists(playlistFilePath)) {
            try {
                List<String> lines = Files.readAllLines(playlistFilePath);
                for (String line : lines) {
                    // Split the line into parts
                    String[] parts = line.split(";");
                    if (parts.length > 1) {
                        // The first part is the playlist name
                        String playlistName = parts[0];
                        Playlist playlist = new Playlist(playlistName);

                        // The rest of the parts are song file paths
                        for (int i = 1; i < parts.length; i++) {
                            String songFilePath = parts[i];
                            File songFile = new File(songFilePath);
                            if (songFile.exists()) {
                                createSongFromFile(songFile, song -> {
                                    // This block of code will be called when the song is ready
                                    // Now you can add the song to the playlist
                                    playlist.addSong(song);

                                    // If you need to update the UI, wrap it in Platform.runLater
                                    Platform.runLater(() -> {
                                        // Update your UI here if necessary
                                    });
                                });
                            } else {
                                System.out.println("File not found: " + songFile.getPath());
                            }
                        }

                        // Add the playlist to the observable list
                        playlists.add(playlist);
                    }
                }

                // Set the observable list to the ListView
                playlistListView.setItems(playlists);
            } catch (IOException e) {
                System.out.println("Error reading playlist file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Playlist file does not exist.");
        }
    }


    private void saveMasterSongList() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SONG_LIST_FILE))) {
            for (Song song : masterSongList) {
                // Write only the absolute file path
                writer.write(song.getFilePath());
                writer.newLine();
                System.out.println("Captured path: " + song.getFilePath());
                System.out.println("Saving path: " + song.getFilePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Call this method when application is closing
    @FXML
    void handleApplicationClose() {
        saveMasterSongList(); // Save the master list to a file
        savePlaylists();
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

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupListViewContextMenu() {
        // Create a Context Menu
        ContextMenu contextMenu = new ContextMenu();

        // Create Menu Items
        MenuItem removeItem = new MenuItem("Remove from Library");
        MenuItem addItemToPlaylist = new MenuItem("Add to Playlist");
        MenuItem removeItemFromPlaylist = new MenuItem("Remove from Playlist");

        contextMenu.getItems().addAll(addItemToPlaylist, removeItemFromPlaylist, removeItem);

        // Set the context menu on the ListView
        songListView.setContextMenu(contextMenu);

        addItemToPlaylist.setOnAction(event -> addSongToPlaylist());
        removeItemFromPlaylist.setOnAction(event -> removeSongFromPlaylist());
        removeItem.setOnAction(event -> {
            Song selectedSong = songListView.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                songObservableList.remove(selectedSong);
            }
        });
    }
    private void addSongToList(File file) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> {
            String title = (String) media.getMetadata().get("title");
            String artist = (String) media.getMetadata().get("artist");
            String album = (String) media.getMetadata().get("album");
            Double duration = media.getDuration().toSeconds();
            Image albumImage = (Image) media.getMetadata().get("image");

            if (title == null || title.isEmpty()) {
                title = file.getName().substring(0, file.getName().lastIndexOf('.'));
            }

            Song song = new Song(title, artist, album, duration, "Unknown Genre", file.getAbsolutePath());
            song.setAlbumImage(albumImage);
            Platform.runLater(() -> {
                songObservableList.add(song);
            });
            mediaPlayer.dispose();
        });
        mediaPlayer.play();  // Note: You might not want to play the song immediately upon adding. Consider removing this line if not needed.
    }

    private void loadPlaylist(Playlist playlist) {
        songListView.setItems(playlist.getSongs());
    }

    @FXML
    private void createPlaylist() {
        String name = promptForPlaylistName("New Playlist");  // This could be input from a dialog
        if(name != null && !name.isEmpty()) {
            Playlist newPlaylist = new Playlist(name);
            playlists.add(newPlaylist);
            // Don't automatically switch to this new playlist in the songListView
            // playlistListView.getSelectionModel().select(newPlaylist);
        }
    }

    @FXML
    private void deletePlaylist() {
        Playlist selected = playlistListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            playlists.remove(selected);
        }
    }

    @FXML
    private void renamePlaylist() {
        Playlist selected = playlistListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String newName = promptForPlaylistName("Rename playlist");
            if (newName != null && !newName.isEmpty()) {
                selected.setName(newName);
                playlistListView.refresh(); // To update the ListView display
            }
        }
    }

    @FXML
    private void addSongToPlaylist() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) return;

        // If no playlists exist, perhaps prompt the user to create one first
        if (playlists.isEmpty()) {
            // Show a message or create a new playlist
            return;
        }

        // Create a ChoiceDialog with the existing playlists
        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlists.get(0), playlists);
        dialog.setTitle("Select Playlist");
        dialog.setHeaderText("Add song to playlist");
        dialog.setContentText("Choose a playlist:");

        // Show the dialog and capture the user's choice
        Optional<Playlist> result = dialog.showAndWait();
        result.ifPresent(playlist -> {
            playlist.addSong(selectedSong);
            playlistListView.refresh(); // update it
            // Update your view, if necessary
        });
    }

    @FXML
    private void removeSongFromPlaylist() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null && selectedPlaylist != null) {
            selectedPlaylist.removeSong(selectedSong);
            // Refresh the playlist view
            loadPlaylist(selectedPlaylist);
        }
    }

    private String promptForPlaylistName(String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText("Enter new playlist name:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null); // Return the string, or null if cancelled
    }

    private void displayMasterLibrary() {
        if (masterSongList.isEmpty()) {
            System.out.println("Master song list is empty.");
            showAlert("Display Master Library", "Master song list is empty.", Alert.AlertType.INFORMATION);
        } else {
            System.out.println("Displaying master song list with " + masterSongList.size() + " songs.");
        }
        songListView.setItems(masterSongList);
        songListView.refresh(); // Force the ListView to refresh its display
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMasterSongList();
        loadPlaylists();
        songListView.setItems(masterSongList); // Display the master list in songListView
        playlistListView.setItems(playlists);

        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPlaylist(newVal); // Load the selected playlist
            } else {
                displayMasterLibrary(); // No playlist selected, show the master library
            }
        });

        setupListViewContextMenu();  // Setup context menu for ListView
        songListView.setItems(songObservableList); // Set the items for the ListView using your song list.

        // Listener for song selection in ListView
        songListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSong, newSong) -> {
            if (newSong == null) return;

            System.out.println("Song selected from ListView: " + newSong.getTrackTitle());

            // Check if there is an existing media player and if the new song is different from the current song
            if (currentPlayer != null && !newSong.equals(currentSong)) {
                System.out.println("Different song selected. Updating button to show play icon.");
                setButtonIcon(playButton, "play.png"); // Set to 'play' because it's a new song selection
            } else if (currentPlayer != null && newSong.equals(currentSong)) {
                // If the selected song is the same as the current and playing, ensure the pause button is shown
                MediaPlayer.Status status = currentPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    setButtonIcon(playButton, "pause.png");
                } else {
                    setButtonIcon(playButton, "play.png");
                }
            } else {
                // No current player or a new player needs to be created
                setButtonIcon(playButton, "play.png");
            }
        });
        // Handle the progress slider interaction for seeking in the current song.
        progressSlider.setOnMousePressed(event -> {
            if (currentPlayer != null) {
                currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        progressSlider.setOnMouseReleased(event -> {
            if (currentPlayer != null) {
                currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        // Drag and Drop handlers for the songListView.
        songListView.setOnDragOver(event -> {
            if (event.getGestureSource() != songListView &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        songListView.setOnDragEntered(event -> {
            if (event.getGestureSource() != songListView &&
                    event.getDragboard().hasFiles()) {
                songListView.setStyle("-fx-border-color: blue;");
            }
        });

        songListView.setOnDragExited(event -> {
            songListView.setStyle(""); // Reset the visual feedback when the drag exits the ListView.
        });

        songListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    if (file.getName().toLowerCase().endsWith(".mp3")) {
                        addSongToList(file);
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Setting up the cell factory to display song titles in the ListView.
        songListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                setText(empty || song == null ? null : song.getTrackTitle());
            }
        });

        // setting up cell factory for playlistListview
        playlistListView.setCellFactory(param -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                setText(empty || playlist == null ? null : playlist.getName());
            }
        });

        // Add a listener to update song info display when a song is selected from the list.
        songListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateSongInfoDisplay(newValue));
    }
}