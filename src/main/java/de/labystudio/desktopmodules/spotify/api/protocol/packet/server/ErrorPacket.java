package de.labystudio.desktopmodules.spotify.api.protocol.packet.server;

import de.labystudio.desktopmodules.spotify.api.protocol.ErrorType;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.SpotifyPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Error packet sent from the api
 *
 * @author LabyStudio
 */
public class ErrorPacket extends SpotifyPacket {

    private final Byte id;

    public ErrorPacket() {
        // Get packet id
        this.id = PacketRegistry.getIdOf(PacketRegistry.RegistryChannel.SERVER, getClass());
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        // No data
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException {
        // No data
    }

    @Override
    public void handlePacket(PacketHandler packetHandler) {
        if (this.id != null) {
            // Convert packet id to error type
            ErrorType type = ErrorType.values()[this.id - 1];

            // Handle error
            packetHandler.handleExecutableError(type);
        }
    }
}
