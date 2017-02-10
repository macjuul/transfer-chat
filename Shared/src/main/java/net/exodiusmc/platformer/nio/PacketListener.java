package net.exodiusmc.platformer.nio;

import net.exodiusmc.platformer.nio.handler.PacketHandlerInterface;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class representing a single listener
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class PacketListener {

	private Set<Class<? extends Packet>> packets;
	private PacketHandlerInterface handler;

	public PacketListener(Class<? extends Packet> type, PacketHandlerInterface handler) {
		this.packets = new HashSet<>();
		this.handler = handler;

		packets.add(type);
	}

	public PacketListener(List<Class<? extends Packet>> types, PacketHandlerInterface handler) {
		this.packets = new HashSet<>();
		this.handler = handler;

		packets.addAll(types);
	}

	/**
	 * Returns the Class subscribed to
	 *
	 * @return Class set
	 */
	public Set<Class<? extends Packet>> getSubscribed() {
		return Collections.unmodifiableSet(packets);
	}

	/**
	 * Returns the callback handler
	 *
	 * @return ServerPacketInboundHandler
	 */
	public PacketHandlerInterface getHandler() {
		return handler;
	}

}
