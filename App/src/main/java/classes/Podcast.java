package classes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Podcast {
    private String podcastName;
    private String description;
    private String host;
    private LocalDate releaseDate;
    private List<Episode> episodes;

    // constructor
    public Podcast(String podcastName, String description, String host, LocalDate releaseDate) {
        this.podcastName = podcastName;
        this.description = description;
        this.host = host;
        this.releaseDate = releaseDate;
        this.episodes = new ArrayList<>();
    }

    // setters getters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void addEpisode(Episode episode) {
        episodes.add(episode);
    }

}
