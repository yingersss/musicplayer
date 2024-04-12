package edu.metrostate;

import classes.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {

    @FXML
    private Label label;

    @FXML
    private Label value;

    // left side column song listview
    @FXML
    private ListView<Song> songListView;

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
    private void handlePlayAction() {
    }
    @FXML
    private void handlePreviousAction() {
    }
    @FXML
    private void handleNextAction() {
    }
    @FXML
    private void handleShuffleAction() {
    }
    @FXML
    private void handleRepeatAction() {
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        // creating new songs
        Song song1 = new Song("My Heart Will Go On", "Celine Dion", "Pop", 280.0, "Let's Talk About Love");
        Song song2 = new Song("All I Want For Christmas Is You", "Mariah Carey", "Christmas", 280.0, "Merry Christmas");
        Song song3 = new Song("I Will Always Love You", "Whitney Houston", "Soul", 271.0, "The Bodyguard: Original Soundtrack");


        // creating playlist
        Playlist yingsPlaylist = new Playlist("Ying's Playlist");

        // creating audiobook
        Audiobook book1 = new Audiobook("Test book 1", "Ying Vang", 66.0, 0);

        // creating podcast
        Podcast JoeRogan = new Podcast("The Joe Rogan Experience", "The offical podcast of comedian Joe Rogan", "Joe Rogan",LocalDate.of(2009, 12, 24));

        // creating episodes

        Episode episode1 = new Episode("Pilot And Introductions", "Joe Rogan", 60.0, "The Very First Episode of Joe Rogan's Podcast", LocalDate.of(2009, 12, 24));

        // adding it into joerogan podcast
        JoeRogan.addEpisode(episode1);

        // creating observableList and populating
        ObservableList<Song> songObservableList = FXCollections.observableArrayList();
        songObservableList.add(song1);
        songObservableList.add(song2);
        songObservableList.add(song3);

        // setting songListView with the items/songs in the songObservableList
        songListView.setItems(songObservableList);
        // cell factory so that it displays string title rather than object reference
        songListView.setCellFactory(param -> new ListCell<>() {
            @Override
            public void updateItem(Song song, boolean empty) {
                // call default implementation
                super.updateItem(song, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(song.getTrackTitle());
                }
            }
        });


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
