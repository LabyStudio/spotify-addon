package de.labystudio.desktopmodules.spotify.api.protocol;

import de.labystudio.desktopmodules.spotify.api.protocol.packet.both.DataPacket;

/**
 * Packet handler callback interface
 *
 * @author LabyStudio
 */
public interface PacketHandler {

    /**
     * Handle the received spotify data
     *
     * @param packet Data packet containing all spotify information
     */
    void handleDataPacket(DataPacket packet);

    /**
     * Handle error of SpotifyAPI.exe
     *
     * @param type The type of the error
     */
    void handleExecutableError(ErrorType type);
}
