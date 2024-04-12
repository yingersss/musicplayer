package classes;
public class Song extends Track{
    private String albumName;
    private String genre;
    private String filePath;

    // constructor
    public Song (String trackTitle, String trackAuthor, String trackGenre, double trackLength, String albumName) {
        super(trackTitle, trackAuthor, trackLength);
        this.albumName = albumName;
        this.genre = trackGenre;
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

    public static Song createFromFilePath(String filePath) {
        // Here you would parse the MP3 file to get metadata like title, artist, etc.
        // For simplicity, we're just using dummy data.
        return new Song("Song Title", "Artist", "Genre", 240, "Album");
    }

}
