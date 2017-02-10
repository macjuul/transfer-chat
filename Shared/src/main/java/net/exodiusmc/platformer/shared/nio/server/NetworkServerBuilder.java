package net.exodiusmc.platformer.shared.nio.server;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import net.exodiusmc.platformer.shared.nio.*;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * NetworkServer builder
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 26-1-2017
 */
public class NetworkServerBuilder {

	protected int port;
	protected char[] token = null;
	protected Logger logger = null;
	protected BiMap<Byte, Class<? extends Packet>> known_packets;
	protected ListMultimap<HookType, Consumer<PacketConnection>> hooks;

	/**
	 * Create a new builder used to construct a NetworkServer
	 *
	 * @param port Server port
	 */
	protected NetworkServerBuilder(int port) {
		this.port = port;
		this.hooks = ArrayListMultimap.create();
		this.known_packets = HashBiMap.create();

		// Register system packets
		packet(-3, PacketSystemAuthentication.class);
		packet(-2, PacketSystemAuthenticationSuccess.class);
		packet(-1, PacketSystemDisconnect.class);
	}

	/**
	 * Use the specified logger for
	 *
	 * @param logger Logger
	 * @return self
	 */
	public NetworkServerBuilder useLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * Setup authentication for this NetworkServer
	 *
	 * @param token Auth token
	 * @return self
	 */
	public NetworkServerBuilder useAuthentication(char[] token) {
		this.token = token;
		return this;
	}

	/**
	 * Register a new packet with the given id and class assigned
	 *
	 * @param id packet id
	 * @param clazz packet class
	 * @return self
	 */
	public NetworkServerBuilder packet(int id, Class<? extends Packet> clazz) {
		this.known_packets.put((byte) id, clazz);
		return this;
	}

	/**
	 * Register an array of packet info objects
	 *
	 * @param pinfos packet info array
	 * @return self
	 */
	public NetworkServerBuilder packets(PacketInfo[] pinfos) {
		for(PacketInfo pinfo : pinfos) {
			this.known_packets.put(pinfo.getId(), pinfo.getPacketClass());
		}
		return this;
	}

	/**
	 * Register a new hook to listen for
	 *
	 * @return self
	 */
	public NetworkServerBuilder hook(HookType type, Consumer<PacketConnection> hook) {
		hooks.put(type, hook);
		return this;
	}

	/**
	 * Use the configured settings to build a new NetworkServer
	 *
	 * @return NetworkServer
	 */
	public NetworkServer build() {
		return new NetworkServer(this);
	}

	/**
	 * <i>Shortcut method.</i><br>
	 * Builds the NetworkServer with {@link #build()} and
	 * calls {@link net.exodiusmc.shared.nio.NetworkInstance#start()}
	 *
	 * @return NetworkServer
	 */
	public NetworkServer buildAndStart() {
		NetworkServer Server = build();

		Server.start();

		return Server;
	}
}
