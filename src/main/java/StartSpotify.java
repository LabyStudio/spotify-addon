import de.labystudio.desktopmodules.spotify.SpotifyAddon;

public class StartSpotify {

    public static void main(String[] args) throws Exception {
        // Start the core with the addon
        Start.main(new String[]{SpotifyAddon.class.getName()});
    }

}
