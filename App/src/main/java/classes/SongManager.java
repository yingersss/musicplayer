package classes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class SongManager {
    private ObservableList<Song> masterSongList = FXCollections.observableArrayList();
    private static final String SONG_LIST_FILE = "songs.txt";

    public ObservableList<Song> getMasterSongList() {
        return masterSongList;
    }

    public void addSongToList(File file, SongReadyCallback callback) {
        if (file != null && file.exists() && file.getName().toLowerCase().endsWith(".mp3")) {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnReady(() -> {
                String title = media.getMetadata().containsKey("title") ? media.getMetadata().get("title").toString() : file.getName().substring(0, file.getName().lastIndexOf('.'));
                String artist = media.getMetadata().containsKey("artist") ? media.getMetadata().get("artist").toString() : "Unknown Artist";
                String album = media.getMetadata().containsKey("album") ? media.getMetadata().get("album").toString() : "Unknown Album";
                Double duration = media.getDuration().toSeconds();
                Image albumImage = media.getMetadata().containsKey("image") ? (Image) media.getMetadata().get("image") : null;
                String genre = media.getMetadata().containsKey("genre") ? media.getMetadata().get("genre").toString() : "Unknown Genre";

                Song song = new Song(title, artist, album, duration, genre, file.getAbsolutePath());
                song.setAlbumImage(albumImage);
                masterSongList.add(song); // Add to the master list

                if (callback != null) {
                    callback.onSongReady(song);
                }

                mediaPlayer.dispose();
            });
            mediaPlayer.play(); // Necessary to load metadata. Consider preparing without playing if possible.
        }
    }

    public void loadMasterSongList() {
        File file = new File(SONG_LIST_FILE);
        if (!file.exists()) {
            System.out.println("Song list file does not exist.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(SONG_LIST_FILE));
            lines.forEach(this::processSongPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processSongPath(String line) {
        System.out.println("Trying to load song from path: " + line);
        File songFile = new File(line);
        if (!songFile.exists()) {
            System.out.println("Song file does not exist: " + line);
            return;
        }

        createSongFromFile(songFile, song -> {
            if (song == null) return;
            Platform.runLater(() -> {
                masterSongList.add(song);
                System.out.println("Song added to masterSongList: " + song.getTrackTitle()); // Debug statement
                System.out.println("Master song list size: " + masterSongList.size()); // Debug statement
            });
        });
    }


    public void saveMasterSongList() {
        System.out.println("Saving master song list to file.");
        try {
            if (!masterSongList.isEmpty()) {
                List<String> lines = new ArrayList<>();
                for (Song song : masterSongList) {
                    lines.add(song.getFilePath());
                    System.out.println("Saving song path: " + song.getFilePath());
                }
                Files.write(Paths.get(SONG_LIST_FILE), lines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createSongFromFile(File file, SongReadyCallback callback) {
        if (file == null || !file.exists()) {
            callback.onSongReady(null); // Return null if the file does not exist
            return;
        }

        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            String title = media.getMetadata().containsKey("title") ?
                    media.getMetadata().get("title").toString() : "Unknown Title";

            String artist = media.getMetadata().containsKey("artist") ?
                    media.getMetadata().get("artist").toString() : "Unknown Artist";

            String album = media.getMetadata().containsKey("album") ?
                    media.getMetadata().get("album").toString() : "Unknown Album";

            Double duration = media.getDuration().toSeconds();

            String genre = media.getMetadata().containsKey("genre") ?
                    media.getMetadata().get("genre").toString() : "Unknown Genre";

            Image albumImage = media.getMetadata().containsKey("image") ?
                    (Image) media.getMetadata().get("image") : null;

            Song song = new Song(title, artist, genre, duration, album, file.getAbsolutePath()); // Correct order and parameters
            if (albumImage != null) {
                song.setAlbumImage(albumImage); // Set album image if available
            }

            // Call the callback with the new song
            if (callback != null) {
                callback.onSongReady(song);
            }

            mediaPlayer.dispose(); // Dispose of the media player immediately after use
        });

        mediaPlayer.setOnError(() -> {
            System.out.println("Error loading media: " + mediaPlayer.getError().getMessage());
            callback.onSongReady(null); // Notify callback about the error
            mediaPlayer.dispose();
        });

        mediaPlayer.play(); // Start loading the media to trigger metadata loading
    }

        private static String getDefaultTitle(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        return pos > 0 ? name.substring(0, pos) : name;
    }
}
