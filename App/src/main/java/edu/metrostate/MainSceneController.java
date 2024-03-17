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
    private ListView<Song> songListView;

    ArrayList<Song> songsList;
    Song currentSong;

    String[] songs;

    private ValueStore store;

    private final String valueFormatString = "Current value: %d";


    public void initialize() {
        // label.setText("Hello, ICS372 JavaFX");
        // hard coding songs into listview
        /*
        ObservableList<Song> songs = FXCollections.observableArrayList(
                new Song("My Heart Will Go On", "Celine Dion", "Pop", 280.0, "Let's Talk About Love")
                // add more songs if needed
        );
         */
        Song song1 = new Song("My Heart Will Go On", "Celine Dion", "Pop", 280.0, "Let's Talk About Love");
        Song song2 = new Song("All I Want For Christmas Is You", "Mariah Carey", "Christmas", 280.0, "Merry Christmas");
        Song song3 = new Song("I Will Always Love you", "Whitney Houston", "Soul", 271.0, "The Bodyguard: Original Soundtrack");

        songsList.add(song1);
        songsList.add(song2);
        songsList.add(song3);

        songListView.getItems().addAll(songsList);
        songListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Song>() {
            @Override
            public void changed(ObservableValue<? extends Song> observableValue, Song song, Song t1) {
                currentSong = songListView.getSelectionModel().getSelectedItem();
            }
        });
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
