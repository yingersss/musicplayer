# Music Player
## Milestone 3
## Code Structure
- 'classes' package: contains the core classes that we listed that include 'Track', 'Song', 'Audiobook','Playlist', 'UIConstructor', and 'TrackRepository'.
- 'edu.metrostate' package: contains MainApp and the MainSceneController where we will be controlling the important bits of the initial UI of the application such as ListView, Buttons that include 'Play', 'Next', 'Back', 'Shuffle', and 'Repeat'. Within the MainSceneController class, I've instantiated objects such as Songs, Audiobooks, and Playlists.
- 'resources': contains the musicplayertest.fxml that we've generated from SceneBuilder and where we hardcoded Strings of song names to put inside the ListView, as well as where we included ImageView into each Button to allow graphics rather than just text. Inside the resources folder also includes the images that we've used.

## Running the Application
To run the application follow these steps:

1. Ensure that you have Java Development Kit (JDK) installed on your system.
2. Extract the zip file.
3. Open the file via an IDE (we are using IntelliJ Community Edition).
4. This application will require JDK version 17, make sure you have that installed.
5. Click on the elephant icon on the top right side if using IntelliJ to synchronize project and dependencies.
6. Ensure that "Music Player" run configuration is selected in the dropdown menu to the left of the green button on the top right side of the toolbar
7. Click the play button, the project should then compile and start the application, showing the skeleton of our application UI.