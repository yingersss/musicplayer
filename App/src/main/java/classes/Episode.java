package classes;

import java.time.LocalDate;

public class Episode extends Track {
    private String description;
    private LocalDate releaseDate;

    // constructor
    public Episode(String trackTitle, String trackAuthor, double trackLength, String description, LocalDate releaseDate) {
        super(trackTitle, trackAuthor, trackLength);
        this.description = description;
        this.releaseDate = releaseDate;
    }

    // getter setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

}
