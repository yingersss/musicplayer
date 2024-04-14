package classes; // temporary package name, change if necessary
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
public class Playlist {
    private final StringProperty name;
    private ObservableList<Song> songs;

    public Playlist(String name) {
        this.name = new SimpleStringProperty(name);
        this.songs = FXCollections.observableArrayList();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String newName) {
        this.name.set(newName);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObservableList<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
        }
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    @Override
    public String toString() {
        return getName();
    }

}
