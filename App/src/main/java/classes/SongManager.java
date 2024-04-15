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

    // creates songs based on metadata retrieved from mp3 file and adds to mastersongList
    public void addSongToList(File file, SongReadyCallback callback) {
        if (file != null && file.exists() && file.getName().toLowerCase().endsWith(".mp3")) {
            Media media = new Media(file.toURI().toString()); // creates media based on file string
            MediaPlayer mediaPlayer = new MediaPlayer(media); // initializes mediaplayer
            mediaPlayer.setOnReady(() -> { // when ready takes the metadata
                String title = media.getMetadata().containsKey("title") ? media.getMetadata().get("title").toString() : file.getName().substring(0, file.getName().lastIndexOf('.'));
                String artist = media.getMetadata().containsKey("artist") ? media.getMetadata().get("artist").toString() : "Unknown Artist";
                String album = media.getMetadata().containsKey("album") ? media.getMetadata().get("album").toString() : "Unknown Album";
                Double duration = media.getDuration().toSeconds();
                Image albumImage = media.getMetadata().containsKey("image") ? (Image) media.getMetadata().get("image") : null;
                String genre = media.getMetadata().containsKey("genre") ? media.getMetadata().get("genre").toString() : "Unknown Genre";

                // song creation
                Song song = new Song(title, artist, album, duration, genre, file.getAbsolutePath());
                song.setAlbumImage(albumImage);
                masterSongList.add(song); // adds to the master list

                if (callback != null) {
                    callback.onSongReady(song);
                }

                mediaPlayer.dispose(); // disposes of mediaplayer as only needed for metadata
            });
            mediaPlayer.setMute(true); // i think this will fix the loud blarind sound at start when loading up lots of songs
            mediaPlayer.play(); // necessary to load metadata
        }
    }

    // method to loadmastersonglist based on a txt file
    public void loadMasterSongList() {
        File file = new File(SONG_LIST_FILE); // new file based on song.txt

        if (!file.exists()) {
            System.out.println("Song list file does not exist."); // debug
            return;
        }

        try { // takes each line puts them into a list of strings lines
            // for each line call processSongPath
            List<String> lines = Files.readAllLines(Paths.get(SONG_LIST_FILE));
            lines.forEach(this::processSongPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // helper method to add songs to mastersonglist
    private void processSongPath(String line) {
        //System.out.println("Trying to load song from path: " + line); // debug

        File songFile = new File(line);
        if (!songFile.exists()) {
            System.out.println("Song file does not exist: " + line); // debug
            return; // do nothing
        }

        // creates songs from files and adds them to mastersonglist
        createSongFromFile(songFile, song -> {
            if (song == null) return;
            Platform.runLater(() -> {
                masterSongList.add(song);
                //System.out.println("Song added to masterSongList: " + song.getTrackTitle()); // debug statement
                //System.out.println("Master song list size: " + masterSongList.size()); // debug statement
            });
        });
    }

    // saves mastersonglist into songs.txt
    public void saveMasterSongList() {
        System.out.println("Saving master song list to file."); // debug
        try {
            if (!masterSongList.isEmpty()) { // mastersonglist isnt empty
                List<String> lines = new ArrayList<>(); // create new list of string that stores each line
                for (Song song : masterSongList) { // for each song in mastersonglist
                    lines.add(song.getFilePath()); // we get the path
                    System.out.println("Saving song path: " + song.getFilePath()); // debug
                }
                Files.write(Paths.get(SONG_LIST_FILE), lines); // we write it to the txt file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // helper method to createSongFromFile
    public static void createSongFromFile(File file, SongReadyCallback callback) {

        if (file == null || !file.exists()) {
            callback.onSongReady(null); // returns null if the file does not exist
            return; // do nothing
        }

        // media creation
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        // when mediaplayer is ready grabs the metadata and stores them
        // if empty then store unknown
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

            // creates new song based on metadata and filepath
            Song song = new Song(title, artist, genre, duration, album, file.getAbsolutePath()); // Correct order and parameters
            if (albumImage != null) {
                song.setAlbumImage(albumImage); // sets album image
            }

            // calls the callback with the new song
            if (callback != null) {
                callback.onSongReady(song);
            }

            mediaPlayer.dispose(); // disposes of the media player immediately after use
        });

        mediaPlayer.setOnError(() -> {
            System.out.println("Error loading media: " + mediaPlayer.getError().getMessage());
            callback.onSongReady(null); // notifies callback about the error
            mediaPlayer.dispose();
        });

        mediaPlayer.setMute(true); // helps to fix blare issue upon start up
        mediaPlayer.play(); // starts loading the media to trigger metadata loading
    }

}
