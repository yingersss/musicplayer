package classes;
public class Song extends Track{
    private String albumName;
    private String genre;

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

}
