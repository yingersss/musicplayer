package classes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PlaylistManager {
    private ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private static final String PLAYLISTS_FILE = "playlists.txt";

    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }

    // method to load playlist from txt file
    public void loadPlaylists() {

        File file = new File(PLAYLISTS_FILE);

        if (!file.exists()) {
            System.out.println("Playlist file does not exist."); // debug
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(PLAYLISTS_FILE)); // reads all lines
            lines.forEach(line -> processLine(line)); // stores each line in a string list and calls processline
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // helper function to load playlist
    private void processLine(String line) {

        String[] parts = line.split(";"); // creates string array with regex ; to differentiate
        if (parts.length == 0) return; // if array -> empty txt file so do nothing

        Playlist playlist = new Playlist(parts[0]); // playlist name will always be first index of array
        Stream.of(parts).skip(1) // converts array to stream and skips the playlist name
                .map(File::new) // converts each string path to file object
                .filter(File::exists) // filters objects that dont exist
                .forEach(songFile -> addSongToPlaylist(songFile, playlist)); // for each file, add to playlist

        playlists.add(playlist); // we then add the newly constructed playlist into a list of playlist
    }

    // method to add a song to playlist
    private void addSongToPlaylist(File songFile, Playlist playlist) {
        SongManager.createSongFromFile(songFile, song -> {
            if (song == null) return;
            Platform.runLater(() -> {
                playlist.addSong(song);
            });
        });
    }

    // method to save playlist to txt file
    public void savePlaylists() {
        try {
            List<String> lines = new ArrayList<>(); // list of string
            for (Playlist playlist : playlists) { // parses through each playlist in playlists list
                StringBuilder line = new StringBuilder(playlist.getName()); // grabs the name of playlist
                for (Song song : playlist.getSongs()) { // for each song in the playlist
                    line.append(";").append(song.getFilePath()); // append ; and song's file path
                }
                lines.add(line.toString()); // then add that to our list of strings
            }
            Files.write(Paths.get(PLAYLISTS_FILE), lines); // write onto playlist.txt
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to create a new playlist
    public void createPlaylist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Playlist name cannot be null or empty.");
        }

        Playlist newPlaylist = new Playlist(name.trim());
        playlists.add(newPlaylist);
    }

    // method to delete a playlist
    public void deletePlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null.");
        }
        playlists.remove(playlist);
    }

    // method to rename a playlist
    public void renamePlaylist(Playlist playlist, String newName) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null.");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New playlist name cannot be null or empty.");
        }
        playlist.setName(newName.trim());
    }
}
