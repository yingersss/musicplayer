package classes;
public class Track {
    private String trackTitle;
    private String trackAuthor;
    private double trackLength;

    // constructor
    public Track(String trackTitle, String trackAuthor, double trackLength) {
        this.trackTitle = trackTitle;
        this.trackAuthor = trackAuthor;
        this.trackLength = trackLength;
    }

    // setters & getters

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }

    public String getTrackAuthor() {
        return trackAuthor;
    }

    public void setTrackAuthor(String trackAuthor) {
        this.trackAuthor = trackAuthor;
    }

    public double getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(double trackLength) {
        this.trackLength = trackLength;
    }
}
