package de.labystudio.desktopmodules.spotify.api.protocol;

import de.labystudio.desktopmodules.spotify.api.protocol.packet.DataPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.NextPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.PlayPausePacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.PreviousPacket;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The registry for all spotify packets
 *
 * @author LabyStudio
 */
public class PacketRegistry {

    private static final Map<Byte, Class<? extends SpotifyPacket>> registry = new HashMap<>();

    static {
        register(0, DataPacket.class);
        register(1, PlayPausePacket.class);
        register(2, NextPacket.class);
        register(3, PreviousPacket.class);
    }

    /**
     * Register a packet type with id and class
     *
     * @param id          Packet id
     * @param packetClass Packet class
     */
    private static void register(int id, Class<? extends SpotifyPacket> packetClass) {
        registry.put((byte) id, packetClass);
    }

    /**
     * Create a packet by the packet id
     *
     * @param id Packet id
     * @return Created packet instance
     */
    public static SpotifyPacket createById(byte id) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends SpotifyPacket> packetClass = registry.get(id);
        if (packetClass == null) {
            return null;
        }
        return packetClass.getConstructor().newInstance();
    }

    /**
     * Get the id of the given packet class
     *
     * @param packetClass The packet class
     * @return Id of the packet
     */
    public static Byte getIdOf(Class<? extends SpotifyPacket> packetClass) {
        for (Map.Entry<Byte, Class<? extends SpotifyPacket>> entry : registry.entrySet()) {
            if (entry.getValue() == packetClass) {
                return entry.getKey();
            }
        }
        return null;
    }

}
