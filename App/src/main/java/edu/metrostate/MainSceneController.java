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
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainSceneController implements Initializable {
    private final SongManager songManager = new SongManager();
    private final PlaylistManager playlistManager = new PlaylistManager();
    private MediaPlayer currentPlayer;
    private Song currentSong;

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
        // retrieves the current list being viewed
        // either mastersonglist or playlist's songlist
        ObservableList<Song> currentListInView = playlistListView.getSelectionModel().isEmpty() ?
                songManager.getMasterSongList() : // uses the master list if no playlist is selected
                playlistListView.getSelectionModel().getSelectedItem().getSongs(); // else use the selected playlist's songs

        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) { // if searchtext is empty
            songListView.setItems(currentListInView); // resets to the full current list if search is cleared
        } else {
            // creates a filtered list based on the current list in view and the search text
            ObservableList<Song> filteredSongs = currentListInView.stream()
                    .filter(song -> song.getTrackTitle().toLowerCase().contains(searchText))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            songListView.setItems(filteredSongs); // updates the songListView to show filteredSongs
        }
    }

    // view all button handler
    @FXML
    private void handleViewAllSongsAction() {
        // if isshuffled is true
        if (isShuffled) {
            handleShuffleAction();
        }

        // resets the observable lists to the master library
        songObservableList.setAll(songManager.getMasterSongList());
        songListView.setItems(songObservableList); // updates songlistview

        // clear selection from playlistlistview
        playlistListView.getSelectionModel().clearSelection();

        // resets shuffledSongsObservableList to be in sync with the master library
        shuffledSongsObservableList = FXCollections.observableArrayList(songManager.getMasterSongList());
        // updates the display to show the master library
        displayMasterLibrary();
    }

    // play button handler
    @FXML
    private void handlePlayAction() {
        // debug print statement
        System.out.println("Play button clicked.");
        // gets selected song
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();

        if (selectedSong == null) {
            System.out.println("No song selected.");
            return; // returns if no songs are selected - do nothing
        }

        // debug prints out song when selecting them
        System.out.println("Selected song: " + selectedSong.getTrackTitle());

        // checks to see if the selected song is different from the currently playing song
        if (currentPlayer != null && !selectedSong.equals(currentSong)) {
            System.out.println("Changing from song: " + (currentSong != null ? currentSong.getTrackTitle() : "none") + " to " + selectedSong.getTrackTitle());
            // if a different song is selected, stops and disposes of the current player
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
            createAndPlayMedia(selectedSong);
        } else if (currentPlayer == null) {
            System.out.println("Debug: MediaPlayer is null, creating new player.");
            createAndPlayMedia(selectedSong);
        } else {
            // if the same song is reselected or is still selected, call toggleplaypause
            togglePlayPause();
        }
    }

    // method to create a media object from given Song and plays it
    // useful for when we are selecting another song and pressing play
    // only creates the media object when we need it
    private void createAndPlayMedia(Song song) {
        // null check
        if (song == null) {
            System.out.println("Debug: Song is null and cannot play.");
            return;
        }

        // debug statement
        System.out.println("Creating new MediaPlayer for: " + song.getFilePath());

        currentSong = song; // sets currentsong to song

        // stops and disposes currentPlayer
        if (currentPlayer != null) {
            System.out.println("Debug: Stopping and disposing current MediaPlayer.");
            currentPlayer.stop();
            currentPlayer.dispose();
        }

        // creation of media and reinitialization of currentPlayer with the new media object
        System.out.println("Debug: Creating MediaPlayer for song: " + song.getFilePath());
        Media media = new Media(new File(song.getFilePath()).toURI().toString());
        currentPlayer = new MediaPlayer(media);
        // sets volume control onReady
        //currentPlayer.setOnReady(this::setupVolumeControl); already calling in setupmediaplayerEvents

        setupMediaPlayerEvents(); // sets up mediaplayerevents
        setupProgressSlider(); // sets up progress slider
        currentPlayer.play(); // plays the media
        System.out.println("Debug: MediaPlayer created and playing."); // debug
        setButtonIcon(playButton, "pause.png"); // switches play button to pause image
    }

    // toggle play and pause method
    // basically method to help with when current song is still being selected
    // and play button is pressed
    private void togglePlayPause() {
        // null check
        if (currentPlayer == null) {
            System.out.println("Debug: MediaPlayer is null in togglePlayPause.");
            return;
        }

        // grabs status of media player
        MediaPlayer.Status status = currentPlayer.getStatus();
        // if status is playing
        if (status == MediaPlayer.Status.PLAYING) {
            System.out.println("Pausing: " + currentSong.getTrackTitle()); // debug
            currentPlayer.pause(); // pauses it
            setButtonIcon(playButton, "play.png"); // switches to play image
        } else { // if not playing
            System.out.println("Resuming: " + currentSong.getTrackTitle()); // debug
            currentPlayer.play(); // plays the media
            setButtonIcon(playButton, "pause.png"); // switches to pause image
        }
    }
    // previous action handler
    @FXML
    private void handlePreviousAction() {
        System.out.println("Previous button clicked."); // debug
        // if songListView is empty
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

    // next button handler
    @FXML
    private void handleNextAction() {
        // exits if songListView is empty
        if (songListView.getItems().isEmpty()) {
            return; // do nothing
        }
        // sets up current and next indexes
        int currentIndex = songListView.getSelectionModel().getSelectedIndex();
        int nextIndex = currentIndex + 1;

        if (nextIndex >= songListView.getItems().size()) { // if next index is last item in list
            switch (repeatMode) { // switch case on repeatMode
                case REPEAT_LIST: // if repeatList
                    nextIndex = 0;  // wraps to the start of the list
                    break;
                case REPEAT_SONG: // if repeatsong
                    nextIndex = currentIndex;  // stays on the current song
                    break;
                default:
                    return;  // no repeat, dont do anything
            }
        }
        songListView.getSelectionModel().select(nextIndex); // selects next song
        playSelectedSong(); // plays selected song
    }

    // shuffle button handler
    @FXML
    private void handleShuffleAction() {
        // determines which list to shuffle
        // if playlistlistview selection is empty chooses mastersongList
        ObservableList<Song> currentListInView = playlistListView.getSelectionModel().isEmpty() ?
                songManager.getMasterSongList() : // master list if no playlist is selected
                playlistListView.getSelectionModel().getSelectedItem().getSongs(); // otherwise chooses playlist songs

        // ensures that the shuffled list is initialized
        if (shuffledSongsObservableList == null) {
            shuffledSongsObservableList = FXCollections.observableArrayList();
        }

        if (!isShuffled) {
            // shuffle mode is off, turns it on
            shuffledSongsObservableList = FXCollections.observableArrayList(currentListInView); // clones currentListinView
            FXCollections.shuffle(shuffledSongsObservableList); // shuffles shuffledSongsObservableList
            songListView.setItems(shuffledSongsObservableList); // set songListView based on shuffledlist
            isShuffled = true; // sets shuffle mode to on
            setButtonIcon(shuffleButton, "shuffle_2.png"); // changes image to shuffle_2
        } else {
            // shuffle mode is on, turns it off and restore original list
            songListView.setItems(currentListInView); // set songlistview to currentlistinview
            isShuffled = false; // sets shuffle mode to off
            setButtonIcon(shuffleButton, "shuffle.png"); // changes image to shuffle
        }
    }

    // repeat button handler
    @FXML
    private void handleRepeatAction() {
        // on click cycles through each repeat mode
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
        updateRepeatButtonIcon(); // updates repeat icon based on the repeat mode
    }

    // update repeat button method
    private void updateRepeatButtonIcon() {
        // debug
        if (repeatButton == null) {
            System.out.println("Repeat button is not initialized!");
            return;
        }

        // switch case based on repeatMode
        // sets button icon based on repeatMode
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

    // handle slider mouse click handler
    @FXML
    private void handleSliderMouseClicked() {
        // not currently being used
    }
    // progress slider handlers
    @FXML
    private void handleSliderMouseReleased() {
        // null check
        if (currentPlayer != null && currentPlayer.getMedia() != null) {
            currentPlayer.seek(Duration.seconds(progressSlider.getValue())); // gets value of where slider is
        }
    }
    @FXML
    private void handleSliderDrag() {
        if (currentPlayer != null && currentPlayer.getMedia() != null) {
            currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
        }
    }

    // method to play the selected song
    private void playSelectedSong() {
        // initializes selectedSong based on currently selected item
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null) return; // null check, do nothing

        if (currentPlayer != null) { // stops and disposes the currentPlayer
            currentPlayer.stop();
            currentPlayer.dispose();
        }

        System.out.println("Creating MediaPlayer for: " + selectedSong.getFilePath()); // debug
        currentSong = selectedSong; // updates currentSong with selectedSong
        System.out.println(selectedSong.getFilePath()); // debug
        Media media = new Media(new File(selectedSong.getFilePath()).toURI().toString()); // media object initialization
        currentPlayer = new MediaPlayer(media); // currentPlayer initialization based on new media object
        setupMediaPlayerEvents(); // sets up media player events
        setupProgressSlider(); // sets up progress slider
        currentPlayer.play(); // plays
    }

    // method to ensure progressslider is properly set up
    // useful for when switching to a new song and playing them ( and previous/next )
    private void setupProgressSlider() {
        if (currentPlayer != null) {
            progressSlider.setValue(0); // sets the initial slider value to 0

            // listener
            currentPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                javafx.util.Duration currentTime = newValue; // updates currentTime with new value
                javafx.util.Duration totalDuration = currentPlayer.getTotalDuration(); // grabs total duration of song

                if (currentPlayer != null && totalDuration != null) {
                    // position calculation of slider
                    // converts currentTime and totalduration -> ms and * by maximum value of progressslider to update slider
                    progressSlider.setValue(currentTime.toMillis() / totalDuration.toMillis() * progressSlider.getMax());
                    updateTimeLabel(currentTime, totalDuration); // calls method to update the time label
                }
            });
        }
    }

    // helper method to update time label
    private void updateTimeLabel(javafx.util.Duration currentTime, javafx.util.Duration totalDuration) {
        // takes currentSeconds and totalseconds
        int currentSeconds = (int) currentTime.toSeconds();
        int totalSeconds = (int) totalDuration.toSeconds();
        // formats and displays mm:ss
        String timeText = String.format("%d:%02d / %d:%02d",
                currentSeconds / 60, currentSeconds % 60,
                totalSeconds / 60, totalSeconds % 60);
        timeLabel.setText(timeText);
    }

    // sets up mediaplayerevents
    private void setupMediaPlayerEvents() {
        if (currentPlayer == null) { // debug
            System.out.println("Debug: MediaPlayer is null in setUpMediaPlayerEvents.");
            return;
        }

        currentPlayer.setOnError(() -> {
            System.out.println("Error with: " + currentSong.getFilePath()); // debug
            System.out.println(currentPlayer.getError().getMessage());
        });

        currentPlayer.setOnPlaying(() -> { // on playing
            System.out.println("Playing: " + currentSong.getFilePath()); // debug
            setButtonIcon(playButton, "pause.png"); // sets button to pause image
        });

        currentPlayer.setOnPaused(() -> { // on paused
            System.out.println("MediaPlayer is paused."); // debug
            setButtonIcon(playButton, "play.png"); // sets button to play
        });

        currentPlayer.setOnEndOfMedia(() -> { // when media file ends
            System.out.println("End of media."); // debug
            switch (repeatMode) { // depending on repeatMode
                case REPEAT_LIST: // if repeatList
                    handleNextAction(); // next button handler
                    break;
                case REPEAT_SONG: // if repeat song mode
                    currentPlayer.seek(Duration.ZERO); // sets currentplayer back to 0
                    currentPlayer.play(); // plays current media
                    // debug
                    System.out.println("Repeating song: " + (currentSong != null ? currentSong.getFilePath() : "unknown path"));
                    break;
                default:
                    handleNextAction(); // next button handler
            }
        });

        currentPlayer.setOnReady(() -> { // on ready listener
            setupVolumeControl(); // sets up volume control
            // sets max value of progresssilder to total duration of current media
            progressSlider.setMax(currentPlayer.getTotalDuration().toSeconds());
        });

        // sets ups listener onto currentTime of currentPlayer
        currentPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) { // checks if slider is not changing (dragged)
                Platform.runLater(() -> {
                    progressSlider.setValue(newValue.toSeconds()); // updates position of progress slider to match
                });
            }
        });
    }

    // method to set up volume control
    private void setupVolumeControl() {
        if (currentPlayer != null) {
            // binds volume property of mediaplyer to value of volume slider
            // this causes volume to be updated when slider is moved
            currentPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));
            // initializes the slider position to the current volume
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

    // call this method when application is closing
    @FXML
    void handleApplicationClose() {
        songManager.saveMasterSongList(); // saves mastersonglist
        playlistManager.savePlaylists(); // saves playlists
    }

    // method to update song description labels in app
    private void updateSongInfoDisplay(Song song) {
        if (song != null) {
            // grabs song's attributes
            songNameInfo.setText(song.getTrackTitle());
            artistNameInfo.setText(song.getTrackAuthor());
            albumNameInfo.setText(song.getAlbumName());
            genreInfo.setText(song.getGenre());

            // sets albumImageView with song's albumImage
            if (song.getAlbumImage() != null) {
                albumImageView.setImage(song.getAlbumImage());
            } else {
                albumImageView.setImage(null); // clear albumimageview if there is no albumImage
            }
        } else { // if song is null
            // clear the labels and the album image if there's no song selected
            songNameInfo.setText("");
            artistNameInfo.setText("");
            albumNameInfo.setText("");
            genreInfo.setText("");
            albumImageView.setImage(null);
        }
    }

    // method to display a popup alert
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // method to set up contextmenu in songListView
    private void setupListViewContextMenu() {
        // creates a contextmenu
        ContextMenu contextMenu = new ContextMenu();

        // creating menu items
        MenuItem removeItem = new MenuItem("Remove from Library");
        MenuItem addItemToPlaylist = new MenuItem("Add to Playlist");
        MenuItem removeItemFromPlaylist = new MenuItem("Remove from Playlist");

        // adds menu items to contextmenu
        contextMenu.getItems().addAll(addItemToPlaylist, removeItemFromPlaylist, removeItem);

        // sets the context menu on to the songListView
        songListView.setContextMenu(contextMenu);

        // on click handlers
        addItemToPlaylist.setOnAction(event -> addSongToPlaylist()); // calls addSongToPlaylist
        removeItemFromPlaylist.setOnAction(event -> removeSongFromPlaylist()); // calls removeSongFromPlaylist
        removeItem.setOnAction(event -> { // removeitem on click
            Song selectedSong = songListView.getSelectionModel().getSelectedItem(); // stores selected song
            if (selectedSong != null) {
                // removes from the correct list based on current state
                // if shuffled -> shuffledsongsobservablelist if not songobservablelist
                ObservableList<Song> currentList = isShuffled ? shuffledSongsObservableList : songObservableList;
                currentList.remove(selectedSong); // remove song
                songManager.getMasterSongList().remove(selectedSong); // always remove it from the master song list
                songListView.refresh(); // updates songListView display
            }
        });
    }

    // method to set up playlistlistview context menu
    @FXML
    private void setupPlaylistListViewContextMenu() {
        // creates context menu
        ContextMenu contextMenu = new ContextMenu();

        // sets up menu items and their on click handlers
        MenuItem createPlaylistItem = new MenuItem("Create Playlist");
        createPlaylistItem.setOnAction(event -> handleCreatePlaylist());

        MenuItem removePlaylistItem = new MenuItem("Remove Playlist");
        removePlaylistItem.setOnAction(event -> handleDeleteSelectedPlaylist());

        MenuItem renamePlaylistItem = new MenuItem("Rename Playlist");
        renamePlaylistItem.setOnAction(event -> handleRenameSelectedPlaylist());

        // adds the items to the contextmenu
        contextMenu.getItems().addAll(createPlaylistItem, removePlaylistItem, renamePlaylistItem);

        // sets the context menu on the playlistListView
        playlistListView.setContextMenu(contextMenu);
    }

    // helper method to create playlists
    @FXML
    private void handleCreatePlaylist() {
        String newName = promptForPlaylistName("New Playlist");
        if (newName != null && !newName.isEmpty()) {
            playlistManager.createPlaylist(newName);
            playlistListView.refresh(); // update the playlist view
        }
    }

    // helper method to delete playlists
    @FXML
    private void handleDeleteSelectedPlaylist() {
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null && showAlertConfirmation("Delete Playlist", "Are you sure you want to delete the playlist: " + selectedPlaylist.getName() + "?")) {
            playlistManager.deletePlaylist(selectedPlaylist);
            playlistListView.refresh(); // update the playlist view
        }
    }

    // helper method to rename playlists
    @FXML
    private void handleRenameSelectedPlaylist() {
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            String newName = promptForPlaylistName("Rename Playlist");
            if (newName != null && !newName.isEmpty()) {
                playlistManager.renamePlaylist(selectedPlaylist, newName);
                playlistListView.refresh(); // update the playlist view
            }
        }
    }

    // method to create and displays confirmation dialog for stuff like deletion
    private boolean showAlertConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    // method to loadplaylists on startup
    private void loadPlaylist(Playlist playlist) {
        songListView.setItems(playlist.getSongs());
    }

    // method to add songs to playlist
    @FXML
    private void addSongToPlaylist() {
        // stores selected song
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong == null)
            return; // do nothing if selectedSong is empty/null

        // If no playlists exist, perhaps prompt the user to create one first
        if (playlistManager.getPlaylists().isEmpty()) {
            handleCreatePlaylist(); // if empty calls to create a playlist
        }

        // creates a ChoiceDialog with the existing playlists
        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlistManager.getPlaylists().get(0), playlistManager.getPlaylists());
        dialog.setTitle("Select Playlist");
        dialog.setHeaderText("Add song to playlist");
        dialog.setContentText("Choose a playlist:");

        // shows the dialog and capture the users choice
        Optional<Playlist> result = dialog.showAndWait();
        result.ifPresent(playlist -> {
            playlist.addSong(selectedSong);
            playlistListView.refresh(); // update playlist listview
        });
    }

    // method to remove songs from playlist
    @FXML
    private void removeSongFromPlaylist() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null && selectedPlaylist != null) {
            selectedPlaylist.removeSong(selectedSong);
            // refreshes the playlist view
            loadPlaylist(selectedPlaylist);
        }
    }

    // method for promptbox when creating new playlist
    private String promptForPlaylistName(String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText("Enter new playlist name:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null); // returns the string or null if cancelled
    }

    // displays masterlibrary -> used for view all songs button or to get back to masterlibrary view
    private void displayMasterLibrary() {
        if (songManager.getMasterSongList().isEmpty()) { // if empty
            System.out.println("Master song list is empty."); // debug
            // shows alert box
            showAlert("Display Master Library", "Master song list is empty.", Alert.AlertType.INFORMATION);
        } else {
            // debug
            System.out.println("Displaying master song list with " + songManager.getMasterSongList().size() + " songs.");
        }
        songListView.setItems(songManager.getMasterSongList()); // sets songlistview to mastersonglist
        songListView.refresh(); // updates listview
    }

    private void setUpSongListView() {

    }

    private void setupDragAndDropHandlers() {
        // drag over handler
        songListView.setOnDragOver(event -> {
            if (event.getGestureSource() != songListView &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // when dragging over songListView
        // border turns blue for clarity
        songListView.setOnDragEntered(event -> {
            if (event.getGestureSource() != songListView &&
                    event.getDragboard().hasFiles()) {
                songListView.setStyle("-fx-border-color: blue;");
            }
        });

        // when drag exits
        // resets the style
        songListView.setOnDragExited(event -> {
            songListView.setStyle("");
        });

        // dragging and dropping handler
        // how we import files
        songListView.setOnDragDropped(event -> {
            // retrieves dragboard from event
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) { // db has files check
                success = true;
                for (File file : db.getFiles()) { // for each file dragged
                    if (file.getName().toLowerCase().endsWith(".mp3")) { // make sure the file dropped is an mp3 file
                        songManager.addSongToList(file, song -> { // adds song to songmasterlist
                            Platform.runLater(() -> {
                                if (!songListView.getItems().contains(song)) {
                                    songListView.getItems().add(song);
                                }
                            });
                        });
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // set up for progress slider handlers
    private void setUpProgressSliderHandlers() {
        // handles the progress slider interaction for seeking in the current song
        progressSlider.setOnMousePressed(event -> { // on mouse press
            if (currentPlayer != null) {
                currentPlayer.seek(Duration.seconds(progressSlider.getValue())); // gets value
            }
        });

        progressSlider.setOnMouseReleased(event -> { // on mouse release
            if (currentPlayer != null) {
                currentPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });
    }

    // set up for song selection handler
    private void setUpSongSelectionHandler() {
        // listener for song selection in ListView
        songListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSong, newSong) -> {
            if (newSong == null)
                return; // do nothing if newSong is null

            System.out.println("Song selected from ListView: " + newSong.getTrackTitle()); // debug print

            // checks if there is an existing media player and if the new song is different from the current song
            if (currentPlayer != null && !newSong.equals(currentSong)) {
                System.out.println("Different song selected. Updating button to show play icon."); // debug
                setButtonIcon(playButton, "play.png"); // set to play image because it's a new song selection
            } else if (currentPlayer != null && newSong.equals(currentSong)) {
                // if the selected song is the same as the current and playing, pause image is shown
                MediaPlayer.Status status = currentPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) { // if mediaplayer is currently playing set to pause
                    setButtonIcon(playButton, "pause.png");
                } else {
                    setButtonIcon(playButton, "play.png"); // else set to play
                }
            } else {
                // else no current player or a new player needs to be created
                // sets playbutton to play image
                setButtonIcon(playButton, "play.png");
            }
        });
    }

    // setting up playlist select handler
    private void setUpPlaylistSelectionHandler() {
        // listener for when selecting a playlist in playlist listview
        // displays playlist's songs onto songlistview
        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPlaylist(newVal); // loads the selected playlist
                if (isShuffled) {
                    // resets shuffle state when a new playlist is selected
                    handleShuffleAction();
                }
            } else {
                displayMasterLibrary(); // if no playlist selected, shows the master library
            }
        });
    }

    // gets song title to listview
    private void populateSongListView() {
        // setting up the cell factory to display song titles in the ListView
        // without this we would just see object references
        songListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                setText(empty || song == null ? null : song.getTrackTitle());
            }
        });
    }

    // puts playlist's names into listview
    private void populatePlaylistListView() {
        // setting up cell factory for playlistListview
        playlistListView.setCellFactory(param -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                setText(empty || playlist == null ? null : playlist.getName());
            }
        });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // loading songs and playlists
        songManager.loadMasterSongList();
        playlistManager.loadPlaylists();

        // debug statements
        System.out.println("Songs loaded: " + songManager.getMasterSongList().size()); // debug
        System.out.println("Playlists loaded: " + playlistManager.getPlaylists().size()); // debug
        System.out.println("MasterSongList size before setting ListView: " + songManager.getMasterSongList().size());

        // sets songListView with mastersonglist
        Platform.runLater(() -> {
            songListView.setItems(songManager.getMasterSongList());
        });

        System.out.println("Items in songListView after setting: " + songListView.getItems().size()); // debug

        // sets playlist listview with playlists
        playlistListView.setItems(playlistManager.getPlaylists());

        setupListViewContextMenu();  // setups context menu for ListView
        setupPlaylistListViewContextMenu(); // setups context menu for playlistview

        songListView.setItems(songObservableList); // set the items for the ListView using songobservablelist

        // handlers set up
        setUpPlaylistSelectionHandler();
        setUpSongSelectionHandler();
        setUpProgressSliderHandlers();
        setupDragAndDropHandlers();

        // populating listviews

        populateSongListView();
        populatePlaylistListView();

        // adds a listener to update song info display when a song is selected from the list
        // displays such things like album, title, artist, etc
        songListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateSongInfoDisplay(newValue));
    }
}