package de.labystudio.desktopmodules.spotify.api.lyrics.source.lrclib;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.LyricsReader;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.LyricsSource;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.lrclib.model.Song;
import de.labystudio.spotifyapi.model.Track;

/**
 * Lrclib lyrics database
 * https://lrclib.net/api/
 *
 * @author *Unknown*
 */
public class LrcLib extends LyricsSource {

    private static final String API_ROOT = "https://lrclib.net/api/";
    private static final String API_SEARCH = API_ROOT + "search";
    

    @Override
    public Lyrics get(Track track) throws Exception {

        URL url = new URL(API_SEARCH + "?track_name=" + track.getName().replace(" ", "+") + "&artist_name=" + track.getArtist().replace(" ", "+") + "&duration=" + track.getLength()/1000);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        //Getting the response code
        int responsecode = conn.getResponseCode();

        if (responsecode != 200) {
            return null;
        } else {
            String inline = "";
            // use utf-8 for the stream
            Scanner scanner = new Scanner(url.openStream(), "UTF-8");
        
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }
            
            //Close the scanner
            scanner.close();



            Song[] songs = GSON.fromJson(inline, Song[].class);


            for(Song song : songs) {
                if( song.name.equalsIgnoreCase(track.getName()) && song.artistName.equalsIgnoreCase(track.getArtist()) ) {
                    if( (song.duration - track.getLength()/1000) <= 1 ) {
                        return new LyricsReader(song.syncedLyrics).readLyrics();
                    }
                }
            }

            return null;
            
        }
    }


    protected String getReferer() {
        return "https://lrclib.net/api/";
    }

    protected String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36";
    }

}
