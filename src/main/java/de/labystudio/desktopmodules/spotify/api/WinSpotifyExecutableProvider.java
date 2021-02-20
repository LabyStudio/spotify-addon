package de.labystudio.desktopmodules.spotify.api;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.security.MessageDigest;

/**
 * Spotify API executable provider
 * Downloads the executable if not available or outdated
 *
 * @author LabyStudio
 */
public class WinSpotifyExecutableProvider {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/71.0.3578.98";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static final String URL_REMOTE = "https://dl.labymod.net/latest/install/spotify/";
    private static final String URL_REMOTE_EXECUTABLE = URL_REMOTE + "SpotifyAPI.exe";
    private static final String URL_REMOTE_HASH = URL_REMOTE + "hash.json";

    private static final int REMOTE_TIMEOUT = 1000 * 2;

    private static final Gson GSON = new Gson();

    private final File destination;

    /**
     * Create provider
     *
     * @param destination Destination of the executable
     */
    public WinSpotifyExecutableProvider(File destination) {
        this.destination = destination;
    }

    /**
     * Make sure the destination path has the latest SpotifyAPI executable
     *
     * @throws Exception Could not make sure it's up to date
     */
    public void provideLatestVersion() throws Exception {
        if (isDownloadRequired()) {
            downloadExecutable();
        }
    }

    /**
     * Download the executable to the destination path
     *
     * @throws IOException Download exception
     */
    private void downloadExecutable() throws IOException {
        FileOutputStream outputStream = new FileOutputStream(this.destination);

        // Open connection
        HttpURLConnection web = (HttpURLConnection) new URL(URL_REMOTE_EXECUTABLE).openConnection();
        web.setRequestProperty("User-Agent", USER_AGENT);

        // Read file
        ReadableByteChannel readableByteChannel = Channels.newChannel(web.getInputStream());
        outputStream.getChannel().transferFrom(readableByteChannel, 0L, Long.MAX_VALUE);
        outputStream.close();
    }

    /**
     * A download of the executable is required when the destination file doesn't exists or if the hash changed of the remote server
     *
     * @return Download required
     */
    private boolean isDownloadRequired() throws Exception {
        // Download for the first time
        if (!this.destination.exists()) {
            return true;
        }

        // Compare remote hash with local hash
        return !fetchRemoteHash().equals(getHashOfFile(this.destination));
    }

    /**
     * Download the latest api executable hash from the remote server
     *
     * @return md5 hash of the remote executable
     * @throws Exception Download and read exception
     */
    private String fetchRemoteHash() throws Exception {
        URLConnection connection = new URL(URL_REMOTE_HASH).openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(REMOTE_TIMEOUT);
        connection.setReadTimeout(REMOTE_TIMEOUT);
        connection.connect();

        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        return GSON.<HashData>fromJson(reader, HashData.class).hash;
    }


    /**
     * Get md5 hash of the given file
     *
     * @param file File to calculate a hash of
     * @return md5 hash of the given file
     * @throws Exception File read exception
     */
    private String getHashOfFile(File file) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return bytesToHex(MessageDigest.getInstance("MD5").digest(bytes)).toLowerCase();
    }


    /**
     * Convert byte array to a hex string
     *
     * @param bytes Byte array input
     * @return Hex string of the given bytes
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static class HashData {
        @SerializedName("spotify_api_exe")
        public String hash;
    }
}
