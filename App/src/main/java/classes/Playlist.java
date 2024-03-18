package classes; // temporary package name, change if necessary
import java.util.ArrayList;
import java.util.List;
public class Playlist {
    private List<Song> songs;
    private String playlistName;

    // constructor
    public Playlist(String playlistName) {
        this.songs = new ArrayList<>();
        this.playlistName = playlistName;
    }

    // getter setter for playlistName
    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    // adding song to playlist
    public void addSong(Song song) {
        songs.add(song);
    }

    // remove
    public void removeSong(Song song) {
        songs.remove(song);
    }

    // print all songs in list
    public void printPlaylist() {
        System.out.println("Playlist songs: \n");
        for ( Song song : songs) {
            System.out.println(song);
        }
    }
}
