package classes;

import javafx.scene.image.Image;

public class Song extends Track{
    private String albumName;
    private String genre;
    private String filePath;
    private Image albumImage;

    // constructor
    public Song (String trackTitle, String trackAuthor, String trackGenre, double trackLength, String albumName, String filePath) {
        super(trackTitle, trackAuthor, trackLength);
        this.albumName = albumName;
        this.genre = trackGenre;
        this.filePath = filePath;
    }

    // getter setter for albumName

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    // getter setter for genre
    public String getGenre() {
        return genre;
    }

    public  void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFilePath() {
        return filePath;
    }

    // filePath setter
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    // Add a setter for the album image
    public void setAlbumImage(Image albumImage) {
        this.albumImage = albumImage;
    }

    // Add a getter for the album image
    public Image getAlbumImage() {
        return albumImage;
    }
    public static Song createFromFilePath(String filePath) {
        // Here you would parse the MP3 file to get metadata like title, artist, etc.
        // For simplicity, we're just using dummy data.
        return new Song("Song Title", "Artist", "Genre", 240, "Album", filePath);
    }

}