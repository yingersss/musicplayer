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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainSceneController implements Initializable {
    private final SongManager songManager = new SongManager();
    private final PlaylistManager playlistManager = new PlaylistManager();
    private MediaPlayer currentPlayer;
    private Song currentSong;
    // private static final String SONG_LIST_FILE = "songs.txt"; // The name of the file to store the song paths

    // left side column song listview
    @FXML
    private TextField searchField;
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
    private final ObservableList<Song> songObservableList = FXCollections.observableArrayList();
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
    private void handleSearchKeyReleased() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            songListView.setItems(songManager.getMasterSongList());  // resets to full list if search is cleared
        } else {
            ObservableList<Song> filteredSongs = FXCollections.observableArrayList();
            for (Song song : songManager.getMasterSongList()) {
                // adds songs that contain the searchText into the filteredSongs list
                if (song.getTrackTitle().toLowerCase().contains(searchText)) {
                    filteredSongs.add(song);
                }
            }
            // we then set the songListView to show filteredSongs
            songListView.setItems(filteredSongs);
        }
    }

    @FXML
    private void handleViewAllSongsAction() {
        // Check if the list is shuffled, if so, reset it
        if (isShuffled) {
            handleShuffleAction(); // This might need to reset the shuffle state
        }

        // Reset the observable lists to the master library
        songObservableList.setAll(songManager.getMasterSongList());
        songListView.setItems(songObservableList); // Update the songListView to show the master library

        // Clear selection from playlistListView to indicate no specific playlist is selected
        playlistListView.getSelectionModel().clearSelection();

        // Reset shuffledSongsObservableList to be in sync with the master library
        shuffledSongsObservableList = FXCollections.observableArrayList(songManager.getMasterSongList());
        // Update the display to show the master library
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
        // Determine which list to shuffle
        ObservableList<Song> currentListInView = playlistListView.getSelectionModel().isEmpty() ?
                songManager.getMasterSongList() : // Master list if no playlist is selected
                playlistListView.getSelectionModel().getSelectedItem().getSongs();

        // Ensure the shuffled list is initialized
        if (shuffledSongsObservableList == null) {
            shuffledSongsObservableList = FXCollections.observableArrayList();
        }

        if (!isShuffled) {
            // Shuffle mode is off, turn it on
            shuffledSongsObservableList = FXCollections.observableArrayList(currentListInView);
            FXCollections.shuffle(shuffledSongsObservableList);
            songListView.setItems(shuffledSongsObservableList);
            isShuffled = true;
            setButtonIcon(shuffleButton, "shuffle_2.png");
        } else {
            // Shuffle mode is on, turn it off and restore original list
            songListView.setItems(currentListInView);
            isShuffled = false;
            setButtonIcon(shuffleButton, "shuffle.png");
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
    private void handleSliderMouseClicked() {
        // not currently being used
    }
    @FXML
    private void handleSliderMouseReleased() {
        if (currentPlayer != null && currentPlayer.getMedia() != null) {
            currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
        }
    }

    @FXML
    private void handleSliderDrag() {
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
    // Call this method when application is closing
    @FXML
    void handleApplicationClose() {
        songManager.saveMasterSongList();
        playlistManager.savePlaylists();
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
                songManager.getMasterSongList().remove(selectedSong); // Remove from the master list
                songObservableList.remove(selectedSong); // Remove from the observable list
                if (isShuffled) {
                    shuffledSongsObservableList.remove(selectedSong); // Remove from the shuffled list if shuffle is on
                    songListView.setItems(shuffledSongsObservableList); // Refresh the list view
                } else {
                    songListView.setItems(songObservableList); // Refresh the list view if shuffle is not on
                }
            }
        });

    }
    @FXML
    private void setupPlaylistListViewContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem createPlaylistItem = new MenuItem("Create Playlist");
        createPlaylistItem.setOnAction(event -> handleCreatePlaylist());

        MenuItem removePlaylistItem = new MenuItem("Remove Playlist");
        removePlaylistItem.setOnAction(event -> handleDeleteSelectedPlaylist());

        MenuItem renamePlaylistItem = new MenuItem("Rename Playlist");
        renamePlaylistItem.setOnAction(event -> handleRenameSelectedPlaylist());


        contextMenu.getItems().addAll(createPlaylistItem, removePlaylistItem, renamePlaylistItem);

        // Set the context menu on the playlistListView
        playlistListView.setContextMenu(contextMenu);
    }

    @FXML
    private void handleCreatePlaylist() {
        String newName = promptForPlaylistName("New Playlist");
        if (newName != null && !newName.isEmpty()) {
            playlistManager.createPlaylist(newName);
            playlistListView.setItems(playlistManager.getPlaylists()); // Refresh the playlist view
        }
    }

    @FXML
    private void handleDeleteSelectedPlaylist() {
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null && showAlertConfirmation("Delete Playlist", "Are you sure you want to delete the playlist: " + selectedPlaylist.getName() + "?")) {
            playlistManager.deletePlaylist(selectedPlaylist);
            playlistListView.setItems(playlistManager.getPlaylists()); // Refresh the playlist view
        }
    }

    @FXML
    private void handleRenameSelectedPlaylist() {
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            String newName = promptForPlaylistName("Rename Playlist");
            if (newName != null && !newName.isEmpty()) {
                playlistManager.renamePlaylist(selectedPlaylist, newName);
                playlistListView.refresh(); // To update the ListView display
            }
        }
    }

    private boolean showAlertConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    private void loadPlaylist(Playlist playlist) {
        songListView.setItems(playlist.getSongs());
    }

    @FXML
    private void addSongToPlaylist() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) return;

        // If no playlists exist, perhaps prompt the user to create one first
        if (playlistManager.getPlaylists().isEmpty()) {
            // Show a message or create a new playlist
            return;
        }

        // Create a ChoiceDialog with the existing playlists
        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlistManager.getPlaylists().get(0), playlistManager.getPlaylists());
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
        if (songManager.getMasterSongList().isEmpty()) {
            System.out.println("Master song list is empty.");
            showAlert("Display Master Library", "Master song list is empty.", Alert.AlertType.INFORMATION);
        } else {
            System.out.println("Displaying master song list with " + songManager.getMasterSongList().size() + " songs.");
        }
        songListView.setItems(songManager.getMasterSongList());
        songListView.refresh(); // Force the ListView to refresh its display
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songManager.loadMasterSongList();
        playlistManager.loadPlaylists();
        // Print statements for debugging
        System.out.println("Songs loaded: " + songManager.getMasterSongList().size());
        System.out.println("Playlists loaded: " + playlistManager.getPlaylists().size());

        System.out.println("MasterSongList size before setting ListView: " + songManager.getMasterSongList().size());
        Platform.runLater(() -> {
            songListView.setItems(songManager.getMasterSongList());
        });

        System.out.println("Items in songListView after setting: " + songListView.getItems().size());

        // Ensure the ListView is populated
        playlistListView.setItems(playlistManager.getPlaylists());

        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPlaylist(newVal); // Load the selected playlist
                if (isShuffled) {
                    // Reset shuffle state when a new playlist is selected
                    handleShuffleAction();
                }
            } else {
                displayMasterLibrary(); // No playlist selected, show the master library
            }
        });

        setupListViewContextMenu();  // Setup context menu for ListView
        setupPlaylistListViewContextMenu(); // setup context menu for playlistview
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
                        songManager.addSongToList(file, song -> {
                            Platform.runLater(() -> {
                                // Update your UI here with the newly added song
                                songListView.getItems().add(song);
                                // Any other UI updates needed after adding a song
                            });
                        });
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