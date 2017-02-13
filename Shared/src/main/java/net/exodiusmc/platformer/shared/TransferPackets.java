package net.exodiusmc.platformer.shared;

import net.exodiusmc.platformer.shared.nio.PacketInfo;
import net.exodiusmc.platformer.shared.packets.ChatSendPacket;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 13/02/2017
 */
public class TransferPackets {

    private TransferPackets() {
    }

    /**
     * A list of all packets used by Transfer
     *
     * @return Packet array
     */
    public static PacketInfo[] list() {
        int i = 0;

        return new PacketInfo[] {
            new PacketInfo(++i, ChatSendPacket.class)
        };
    }
}
