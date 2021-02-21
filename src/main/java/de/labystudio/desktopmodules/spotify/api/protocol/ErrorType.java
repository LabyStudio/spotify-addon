package de.labystudio.desktopmodules.spotify.api.protocol;

public enum ErrorType {
    SPOTIFY_NOT_OPEN(1, "Spotify is not open"),
    SPOTIFY_PROCESS_INVALID(2, "Invalid Spotify process"),
    NO_CHROME_MODULE(3, "An internal module is missing"),
    CAN_NOT_OPEN_PROCESS(4, "Can't open Spotify process"),
    CAN_NOT_FIND_ADDRESS(5, "SpotifyAPI is outdated"),
    CAN_NOT_FIND_TRACK_ID(6, "Could not find track id");

    private final int code;
    private final String message;

    ErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
