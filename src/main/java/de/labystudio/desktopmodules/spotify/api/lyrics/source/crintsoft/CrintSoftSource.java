package de.labystudio.desktopmodules.spotify.api.lyrics.source.crintsoft;


import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.LyricsReader;
import de.labystudio.desktopmodules.spotify.api.lyrics.source.LyricsSource;
import de.labystudio.spotifyapi.model.Track;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Crintsoft lyrics database
 * https://crintsoft.com/
 */
public class CrintSoftSource extends LyricsSource {

    private static final String URL = "http://search.crintsoft.com/searchlyrics.htm";
    private static final String CLIENT_TAG = "client=\"MiniLyrics\"";
    private static final String SEARCH_QUERY_BASE = "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>";
    private static final String SEARCH_QUERY_PAGE = " RequestPage='%d'";
    private static final byte[] MAGIC_KEY = "Mlv1clt4.0".getBytes();

    @Override
    public Lyrics get(Track track) throws Exception {
        String artist = track.getArtist();

        // Remove multiple artist names
        if (artist.contains("&"))
            artist = artist.split("&")[0];

        // Create query string
        String searchQuery = String.format(SEARCH_QUERY_BASE, artist, track.getName(), CLIENT_TAG + String.format(SEARCH_QUERY_PAGE, 0));

        // Make search query and decrypt it
        String encrypted = request(URL, "", assembleQuery(searchQuery.getBytes(StandardCharsets.UTF_8)));
        byte[] decrypted = decryptResult(encrypted);

        // Convert decrypted data to a crintsoft track list
        List<CrintSoftTrack> tracks = binary2List(decrypted);

        // Iterate all tracks
        for (CrintSoftTrack crintSoftTrack : tracks) {
            String trackName = crintSoftTrack.getName();
            String trackArtist = crintSoftTrack.getArtist();

            // Skip unknown tracks
            if (trackName == null || trackArtist == null) {
                continue;
            }

            // Remove additional text in the track name and artist name
            if (trackName.contains(" (")) {
                trackName = trackName.split(" \\(")[0];
            }
            if (trackArtist.contains(" &")) {
                trackArtist = trackArtist.split(" &")[0];
            }

            // Validate that we have to track we requested
            if (trackName.equalsIgnoreCase(track.getName()) && trackArtist.equalsIgnoreCase(artist)) {
                return load(crintSoftTrack);
            }
        }

        // Could not find anything
        return null;
    }

    /**
     * Download the lyrics file and read it
     *
     * @param track Crintsoft track result entry
     * @return Lyrics containing all voice lines
     * @throws Exception Download exception
     */
    private Lyrics load(CrintSoftTrack track) throws Exception {
        // Open connection to lyrics file
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(track.getUrl()).openConnection();

        // Check response code
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode / 100 == 2) {
            // Use the lyrics reader to parse the file
            return new LyricsReader(httpURLConnection.getInputStream()).readLyrics();
        }

        // Could not download the lyrics file from the server
        return null;
    }

    /**
     * Convert the binary result to a track list
     *
     * @param bytes Decrypted binary result from crintsoft
     * @return Track list
     */
    private List<CrintSoftTrack> binary2List(byte[] bytes) {
        byte[] magicStartKey = "server_url".getBytes();

        // Temporary decoded track storage
        CrintSoftTrack currentTrack = null;

        // Index storage
        boolean setup = true;
        int matchingScore = 0;
        int valueIndex = 0;

        // Temporary byte buffer
        List<Byte> keyValueBuffer = new ArrayList<>();

        // Decoded track list
        List<CrintSoftTrack> tracks = new ArrayList<>();

        // Iterate all bytes
        for (byte b : bytes) {
            if (setup) {
                // Find the magic key to start reading
                if (b == magicStartKey[matchingScore]) {
                    matchingScore++;

                    // The magic key is correct, start reading
                    if (matchingScore == magicStartKey.length) {
                        setup = false;
                    }
                } else {
                    // The magic key is not correct, next bytes
                    matchingScore = 0;
                }
            } else {
                if (b == 0) {
                    // Convert temporary byte buffer to string
                    byte[] array = new byte[keyValueBuffer.size()];
                    for (int t = 0; t < array.length; t++) {
                        array[t] = keyValueBuffer.get(t);
                    }
                    String value = new String(array);

                    // Clear the byte buffer
                    keyValueBuffer.clear();

                    // Extract lyrics url from value
                    if (value.contains("/") && value.contains(".lrc")) {
                        // Create new track object
                        currentTrack = new CrintSoftTrack("http://search.crintsoft.com/l/" + value);

                        // Add track to list
                        tracks.add(currentTrack);

                        // Reset index
                        valueIndex = 0;
                    } else {
                        if (currentTrack != null && !value.equals("artist") && valueIndex == 0) {
                            // Extract artist name
                            currentTrack.setArtist(value);

                            // Next index
                            valueIndex++;
                        } else if (currentTrack != null && !value.equals("title") && valueIndex == 1) {
                            // Extract track name
                            currentTrack.setName(value);

                            // Next index
                            valueIndex++;
                        }
                    }
                } else {
                    // Add byte to buffer
                    keyValueBuffer.add(b);
                }
            }
        }

        // Return all parsed tracks
        return tracks;
    }

    /**
     * Add MD5 and encrypts search query
     *
     * @param value Search query in bytes
     * @return Assembled search query
     */
    private byte[] assembleQuery(byte[] value) throws NoSuchAlgorithmException, IOException {
        // Create the variable POG to be used in a dirt code
        byte[] pog = new byte[value.length + MAGIC_KEY.length]; //TODO Give a better name then POG

        // POG = XMLQuery + Magic Key
        System.arraycopy(value, 0, pog, 0, value.length);
        System.arraycopy(MAGIC_KEY, 0, pog, value.length, MAGIC_KEY.length);

        // POG is hashed using MD5
        byte[] pog_md5 = MessageDigest.getInstance("MD5").digest(pog);

        //TODO Thing about using encryption or k as 0...
        // Prepare encryption key
        int j = 0;
        for (byte b : value) {
            j += b;
        }
        int k = (byte) (j / value.length);

        // Value is encrypted
        for (int m = 0; m < value.length; m++)
            value[m] = (byte) (k ^ value[m]);

        // Prepare result code
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        // Write Header
        result.write(0x02);
        result.write(k);
        result.write(0x04);
        result.write(0x00);
        result.write(0x00);
        result.write(0x00);

        // Write Generated MD5 of POG problaby to be used in a search cache
        result.write(pog_md5);

        // Write encrypted value
        result.write(value);

        // Return magic encoded query
        return result.toByteArray();
    }

    /**
     * Decrypt the entire result into bytes
     *
     * @param value Encrypted string result from crintsoft
     * @return Decrypted string in bytes
     */
    private byte[] decryptResult(String value) {
        // Get Magic key value
        char magickey = value.charAt(1);

        // Prepare output
        ByteArrayOutputStream neomagic = new ByteArrayOutputStream();

        // Decrypts only the XML
        for (int i = 22; i < value.length(); i++)
            neomagic.write((byte) (value.charAt(i) ^ magickey));

        // Return value
        return neomagic.toByteArray();
    }

    @Override
    protected String getReferer() {
        return "http://crintsoft.com";
    }

    @Override
    protected String getUserAgent() {
        return "MiniLyrics4Android";
    }
}
