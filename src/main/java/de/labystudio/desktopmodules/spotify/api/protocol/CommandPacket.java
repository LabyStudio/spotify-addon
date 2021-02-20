package de.labystudio.desktopmodules.spotify.api.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Command packets are packets without content that only send a signal to the api
 *
 * @author LabyStudio
 */
public abstract class CommandPacket extends SpotifyPacket {

    @Override
    public void write(DataOutputStream outputStream) {
        // No data
    }

    @Override
    public void read(DataInputStream inputStream) {
        // No data
    }

    @Override
    public void handlePacket(PacketHandler packetHandler) {
        // Can't receive this packet
    }

}
