package de.labystudio.desktopmodules.spotify.api.lyrics.source.music163;

import de.labystudio.desktopmodules.spotify.api.Track;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.LyricsReader;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.LyricsSource;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.music163.model.LyricResponse;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.music163.model.QueryResponse;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.music163.model.Song;

/**
 * Music163 lyrics database
 * https://music.163.com/
 *
 * @author LabyStudio
 */
public class Music163Source extends LyricsSource {

    private static final String API_ROOT = "http://music.163.com/api/";
    private static final String API_SEARCH = API_ROOT + "search/pc?offset=0&total=true&limit=100&type=1&s=%s";
    private static final String API_LYRIC = API_ROOT + "song/lyric?id=%s&lv=1&kv=1&tv=-1";

    @Override
    public Lyrics get(Track track) throws Exception {
        String json = request(API_SEARCH, track.getName() + " " + track.getArtist(), null);
        QueryResponse queryResponse = GSON.fromJson(json, QueryResponse.class);

        // Iterate all query results
        for (Song song : queryResponse.result.songs) {

            // Has matching track name
            if (song.name.equalsIgnoreCase(track.getName())) {

                // Has matching artist name
                if (song.artists != null && song.artists.length != 0 && song.artists[0].name.equalsIgnoreCase(track.getArtist())) {
                    return loadLyrics(song);
                }
            }
        }

        // Could not find anything
        return null;
    }

    /**
     * Download the lyrics file and read it
     *
     * @param song Music163 song result entry
     * @return Lyrics containing all voice lines
     * @throws Exception Download exception
     */
    private Lyrics loadLyrics(Song song) throws Exception {
        String json = request(API_LYRIC, String.valueOf(song.id), null);
        LyricResponse lyricResponse = GSON.fromJson(json, LyricResponse.class);

        // Wrong format
        if (lyricResponse.lrc == null || lyricResponse.lrc.lyric == null)
            return null;

        return new LyricsReader(lyricResponse.lrc.lyric).readLyrics();
    }

    protected String getReferer() {
        return "http://music.163.com/";
    }

    protected String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36";
    }

}
