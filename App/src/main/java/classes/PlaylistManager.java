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

    public void loadPlaylists() {
        File file = new File(PLAYLISTS_FILE);
        if (!file.exists()) {
            System.out.println("Playlist file does not exist.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(PLAYLISTS_FILE));
            lines.forEach(line -> processLine(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(";");
        if (parts.length == 0) return;

        Playlist playlist = new Playlist(parts[0]);
        Stream.of(parts).skip(1) // Skip the playlist name
                .map(File::new)
                .filter(File::exists)
                .forEach(songFile -> addSongToPlaylist(songFile, playlist));

        playlists.add(playlist);
    }

    private void addSongToPlaylist(File songFile, Playlist playlist) {
        SongManager.createSongFromFile(songFile, song -> {
            if (song == null) return;
            Platform.runLater(() -> {
                playlist.addSong(song);
            });
        });
    }


    public void savePlaylists() {
        try {
            List<String> lines = new ArrayList<>();
            for (Playlist playlist : playlists) {
                StringBuilder line = new StringBuilder(playlist.getName());
                for (Song song : playlist.getSongs()) {
                    line.append(";").append(song.getFilePath());
                }
                lines.add(line.toString());
            }
            Files.write(Paths.get(PLAYLISTS_FILE), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to create a new playlist
    public void createPlaylist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Playlist name cannot be null or empty.");
        }
        Playlist newPlaylist = new Playlist(name.trim());
        playlists.add(newPlaylist);
    }

    // Method to delete a playlist
    public void deletePlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null.");
        }
        playlists.remove(playlist);
    }

    // Method to rename a playlist
    public void renamePlaylist(Playlist playlist, String newName) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null.");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New playlist name cannot be null or empty.");
        }
        playlist.setName(newName.trim());
        // If your Playlist class is properly bound to the UI,
        // the UI will automatically reflect the name change.
        // Otherwise, you may need to refresh or update the view.
    }
}
