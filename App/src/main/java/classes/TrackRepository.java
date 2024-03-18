package classes;

import java.util.List;
public interface TrackRepository {

    // get all tracks
    List<Track> getAllTracks();

    // add track
    void addTrack(Track track);

    // remove track
    void removeTrack(Track track);

    // delete track
    void deleteTrack(Track track);
}
