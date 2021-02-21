package de.labystudio.desktopmodules.spotify.api.protocol;

import de.labystudio.desktopmodules.spotify.api.protocol.packet.SpotifyPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.both.DataPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.client.NextPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.client.PlayPausePacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.client.PreviousPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.server.ErrorPacket;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry.RegistryChannel.CLIENT;
import static de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry.RegistryChannel.SERVER;

/**
 * The registry for all spotify packets
 *
 * @author LabyStudio
 */
public class PacketRegistry {

    private static final Map<Byte, Class<? extends SpotifyPacket>> registryClient = new HashMap<>();
    private static final Map<Byte, Class<? extends SpotifyPacket>> registryServer = new HashMap<>();

    static {
        // Register server packets
        register(SERVER, 0, DataPacket.class);

        // Register error types
        for (ErrorType type : ErrorType.values()) {
            register(SERVER, type.getCode(), ErrorPacket.class);
        }

        // Register client packets
        register(CLIENT, 0, DataPacket.class);
        register(CLIENT, 1, PlayPausePacket.class);
        register(CLIENT, 2, NextPacket.class);
        register(CLIENT, 3, PreviousPacket.class);
    }

    /**
     * Register a packet type with id and class
     *
     * @param channel     The registry channel type (Outgoing packet or incoming packet)
     * @param id          Packet id
     * @param packetClass Packet class
     */
    private static void register(RegistryChannel channel, int id, Class<? extends SpotifyPacket> packetClass) {
        Map<Byte, Class<? extends SpotifyPacket>> registry = channel == CLIENT ? registryClient : registryServer;
        registry.put((byte) id, packetClass);
    }

    /**
     * Create a packet by the packet id
     *
     * @param channel The registry channel type (Outgoing packet or incoming packet)
     * @param id      Packet id
     * @return Created packet instance
     */
    public static SpotifyPacket createById(RegistryChannel channel, byte id) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<Byte, Class<? extends SpotifyPacket>> registry = channel == CLIENT ? registryClient : registryServer;
        Class<? extends SpotifyPacket> packetClass = registry.get(id);
        if (packetClass == null) {
            return null;
        }
        return packetClass.getConstructor().newInstance();
    }

    /**
     * Get the id of the given packet class
     *
     * @param channel     The registry channel type (Outgoing packet or incoming packet)
     * @param packetClass The packet class
     * @return Id of the packet
     */
    public static Byte getIdOf(RegistryChannel channel, Class<? extends SpotifyPacket> packetClass) {
        Map<Byte, Class<? extends SpotifyPacket>> registry = channel == CLIENT ? registryClient : registryServer;
        for (Map.Entry<Byte, Class<? extends SpotifyPacket>> entry : registry.entrySet()) {
            if (entry.getValue() == packetClass) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static enum RegistryChannel {
        SERVER,
        CLIENT
    }

}
