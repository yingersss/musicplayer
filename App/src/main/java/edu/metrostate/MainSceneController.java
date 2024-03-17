package edu.metrostate;

import classes.*;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.ArrayList;

public class MainSceneController implements ValueChangedListener {

    @FXML
    private Label label;

    @FXML
    private Label value;

    @FXML
    private MainToolBar mainToolBar;
    @FXML
    private ListView<String> songListView;

    //ArrayList<Song> songsList;
    //ArrayList<String> songTitles;
    //Song currentSong;

    private ValueStore store;

    private final String valueFormatString = "Current value: %d";


    public void initialize() {
        // creating new songs
        Song song1 = new Song("My Heart Will Go On", "Celine Dion", "Pop", 280.0, "Let's Talk About Love");
        Song song2 = new Song("All I Want For Christmas Is You", "Mariah Carey", "Christmas", 280.0, "Merry Christmas");
        Song song3 = new Song("I Will Always Love You", "Whitney Houston", "Soul", 271.0, "The Bodyguard: Original Soundtrack");

        // creating tracks
        Track track1 = new Track("Track 1", "Ying Vang", 30.0);

        // creating playlist
        Playlist yingsPlaylist = new Playlist("Ying's Playlist");

        // creating audiobook
        Audiobook book1 = new Audiobook("Test book 1", "Ying Vang", 66.0, 0);



        /* wasn't really working as intended not quite sure how to populate listview with objects or even strings
        // adding songs to list
        songsList.add(song1);
        songsList.add(song2);
        songsList.add(song3);

        // putting the titles of each song into a tracktitle list
        for(Song song : songsList) {
            songTitles.add(song.getTrackTitle());
        }

        // putting tracktitle list into listview so we can view the songs
        songListView.getItems().addAll(songTitles);
         */
    }

    public void setValueStore(ValueStore store) {
        this.store = store;
        if (this.store != null) {
            this.store.registerValueChangeListener(this);
        }
    }

    @Override
    public void onValueChange(int newValue) {
        value.setText(String.format(valueFormatString, newValue));
    }
}
