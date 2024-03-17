package edu.metrostate;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class MainToolBar extends ToolBar {

    //Button addButton;

    //Button subtractButton;
    Button playButton;
    Button nextButton;
    Button backButton;
    Button shuffleButton;
    Button repeatButton;

    public MainToolBar() {
        /* dont really need any of this, just keeping as comments for future references
        // just testing out graphics and buttons

        // next button
        nextButton = new Button("Next");
        setButtonImage(nextButton, "file:App/UI_IMAGES/next.png");

        // back button
        backButton = new Button("Back");
        setButtonImage(backButton, "file:App/UI_IMAGES/back.png");

        // shuffle button
        shuffleButton = new Button("Shuffle");
        setButtonImage(shuffleButton, "file:App/UI_IMAGES/shuffle.png");

        // repeat button
        repeatButton = new Button("Repeat");
        setButtonImage(repeatButton,"file:App/UI_IMAGES/repeat.png");

        // play button settings
        playButton = new Button();
        setButtonImage(playButton, "file:App/UI_IMAGES/play.png");
        getItems()
                .addAll(shuffleButton, backButton, playButton, nextButton,  repeatButton );
    }

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
         */

    }
}
