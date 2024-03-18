package edu.metrostate;

import classes.*;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class MainSceneController implements ValueChangedListener {

    @FXML
    private Label label;

    @FXML
    private Label value;

    // left side column song listview
    @FXML
    private ListView<String> songListView;

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


        // creating playlist
        Playlist yingsPlaylist = new Playlist("Ying's Playlist");

        // creating audiobook
        Audiobook book1 = new Audiobook("Test book 1", "Ying Vang", 66.0, 0);


        /*
        // wasn't really working as intended not quite sure how to populate listview with objects or even strings
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

    // method to set a button to be round as well as to set images within them
    public void setButtonImage (Button button, String imgFilePath) {
        // button style
        String buttonStyle = "-fx-background-radius: 5em; " +
                "-fx-min-width: 40px; " +
                "-fx-min-height: 40px; " +
                "-fx-max-width: 40px; " +
                "-fx-max-height: 40px;";

        button.setPrefSize(50, 50);
        Image img = new Image(imgFilePath);
        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.fitHeightProperty().bind(button.heightProperty());
        view.fitWidthProperty().bind(button.widthProperty());
        button.setGraphic(view);
        button.setContentDisplay(ContentDisplay.CENTER);
        view.setTranslateX(2); // only if needed
        button.setStyle(buttonStyle);
    }
}
