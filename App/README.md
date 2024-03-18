# Milestone 3


## Code Structure
- 'classes' package: contains the core classes that we listed that include 'Track', 'Song', 'Audiobook','Playlist', 'UIConstructor', and 'TrackRepository'.
- 'edu.metrostate' package: contains MainApp and the MainSceneController where we will be controlling the important bits of the initial UI of the application such as ListView, Buttons that include 'Play', 'Next', 'Back', 'Shuffle', and 'Repeat'. Within the MainSceneController class, I've instantiated objects such as Songs, Audiobooks, and Playlists.
- 'resources': contains the musicplayertest.fxml that we've generated from SceneBuilder and where we hardcoded Strings of song names to put inside the ListView, as well as where we included ImageView into each Button to allow graphics rather than just text. Inside the resources folder also includes the images that we've used.

## Running the Application
To run the application follow these steps:

1. Ensure that you have Java Development Kit (JDK) installed on your system.
2. After extracting the file, open the file via an IDE (we are using IntelliJ Community Edition).
3. Navigate to musicplayer\App\src\main\java\edu\metrostate where you will find the MainApp class.
4. Select MainApp and run it.
5. The application should then start and give you the very minimal bare bones of our application UI.