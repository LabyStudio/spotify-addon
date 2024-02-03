package de.labystudio.desktopmodules.spotify.api.lyrics;

import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.LyricsSource;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.crintsoft.CrintSoftSource;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.music163.Music163Source;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.lrclib.LrcLib;
import de.labystudio.spotifyapi.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * The lyrics provider can search lyrics data for a song in multiple online databases
 *
 * @author LabyStudio
 */
public class LyricsProvider {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * A list of all lyrics databases
     */
    private final List<LyricsSource> sources = new ArrayList<>();

    /**
     * Adds all sources
     */
    public LyricsProvider() {
        this.sources.add(new LrcLib());
        this.sources.add(new Music163Source());
        this.sources.add(new CrintSoftSource());
    }

    /**
     * Search in all databases for the lyrics of the given track asynchronously
     *
     * @param track    The track to find the lyrics to
     * @param callback Callback for the lyrics if available (Lyrics can be null)
     */
    public void requestAsync(Track track, Consumer<Lyrics> callback) {
        // Execute in thread
        this.executor.execute(() -> {
            // Request lyrics of track
            Lyrics lyric = request(track);

            // Call the lyrics callback
            callback.accept(lyric);
        });
    }

    /**
     * Search in all databases for the lyrics of the given track
     *
     * @param track The track to find the lyrics to
     * @return The lyrics (Can be null if there is no result)
     */
    public Lyrics request(Track track) {
        // Search in all databases
        for (LyricsSource lyricSource : this.sources) {
            try {
                // Find lyrics for this database
                Lyrics lyric = lyricSource.get(track);

                // Return if available
                if (lyric != null) {
                    return lyric;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // No lyrics found
        return null;
    }


}
