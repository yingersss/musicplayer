package classes;

public class Audiobook extends Track{
    private double bookmarkIndex;

    // constructor
    public Audiobook(String trackTitle, String trackAuthor, double trackLength, double bookmarkIndex) {
        super(trackTitle, trackAuthor, trackLength);
        this.bookmarkIndex = bookmarkIndex;
    }
}
