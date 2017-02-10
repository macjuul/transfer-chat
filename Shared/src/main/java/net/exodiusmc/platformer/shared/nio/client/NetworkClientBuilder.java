package net.exodiusmc.platformer.shared.nio.client;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import net.exodiusmc.platformer.shared.nio.*;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * NetworkClient builder
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 26-1-2017
 */
public class NetworkClientBuilder {

	protected String ip;
	protected int port;
	protected String identity;
	protected char[] token = null;
	protected Logger logger = null;
	protected BiMap<Byte, Class<? extends Packet>> known_packets;
	protected ListMultimap<HookType, Consumer<PacketConnection>> hooks;

	/**
	 * Create a new builder used to construct a NetworkClient
	 *
	 * @param ip Server ip
	 * @param port Server port
	 * @param identity Client identity (name)
	 */
	protected NetworkClientBuilder(String ip, int port, String identity) {
		this.ip = ip;
		this.port = port;
		this.identity = identity;
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
	public NetworkClientBuilder useLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * Supply an authentication token. This token is required when
	 * the server has authentication enabled.
	 *
	 * @param token Auth token
	 * @return self
	 */
	public NetworkClientBuilder authenticate(char[] token) {
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
	public NetworkClientBuilder packet(int id, Class<? extends Packet> clazz) {
		this.known_packets.put((byte) id, clazz);
		return this;
	}

	/**
	 * Register an array of packet info objects
	 *
	 * @param pinfos packet info array
	 * @return self
	 */
	public NetworkClientBuilder packets(PacketInfo[] pinfos) {
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
	public NetworkClientBuilder hook(HookType type, Consumer<PacketConnection> hook) {
		hooks.put(type, hook);
		return this;
	}

	/**
	 * Use the configured settings to build a new NetworkClient
	 *
	 * @return NetworkClient
	 */
	public NetworkClient build() {
		return new NetworkClient(this);
	}

	/**
	 * <i>Shortcut method.</i><br>
	 * Builds the NetworkClient with {@link #build()} and
	 * calls {@link NetworkInstance#start()}
	 *
	 * @return NetworkClient
	 */
	public NetworkClient buildAndStart() {
		NetworkClient client = build();

		client.start();

		return client;
	}
}
