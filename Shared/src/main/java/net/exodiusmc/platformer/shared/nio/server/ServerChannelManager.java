package net.exodiusmc.platformer.shared.nio.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.exodiusmc.platformer.shared.nio.exception.NioNetworkException;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandlerInterface;
import net.exodiusmc.platformer.shared.nio.handler.PacketResponseHandler;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandler;
import net.exodiusmc.platformer.shared.nio.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Serverside implementation of the ChannelManager
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class ServerChannelManager extends ChannelManager {

	private List<PacketListener> listeners;
	private NetworkServer server;
	private List<PacketConnection> pending;
	private Map<String, PacketConnection> connections;

	/**
	 * Create a new ChannelManager for the given NetworkInstance
	 *
	 * @param parent NetworkInstance
	 */
	public ServerChannelManager(NetworkServer parent) {
		super(parent, parent.builder.hooks, parent.builder.known_packets);

		this.listeners = new ArrayList<>();
		this.connections = new HashMap<>();
		this.pending = new ArrayList<>();

		// Store the NetworkClient
		server = parent;

		// Disconnect handling
		subscribe(PacketSystemDisconnect.class, (_packet, con) -> {
			PacketSystemDisconnect packet = (PacketSystemDisconnect) _packet;

			NioUtil.nettyLog(server.logger(), "** Channel " + con.channel() +
				" disconnected: " + packet.getDisconnectMessage());
		});
	}

	/**
	 * Subscribe a listener of the specified arguments
	 *
	 * @param types Packet Types
	 * @param handler Handler
	 * @return ServerPacketListener
	 */
	public PacketListener subscribe(List<Class<? extends Packet>> types, PacketHandler handler) {
		PacketListener listener = new PacketListener(types, handler);

		listeners.add(listener);

		return listener;
	}

	/**
	 * Subscribe a listener of the specified arguments
	 *
	 * @param types Packet Types
	 * @param handler Handler
	 * @return ServerPacketListener
	 */
	public PacketListener subscribeRespondable(List<Class<? extends Packet>> types, PacketResponseHandler handler) {
		PacketListener listener = new PacketListener(types, handler);

		listeners.add(listener);

		return listener;
	}

	/**
	 * Subscribe a listener of the specified arguments
	 *
	 * @param type Class<? extends Packet>
	 * @param handler Listener
	 * @return ServerPacketListener
	 */
	public PacketListener subscribe(Class<? extends Packet> type, PacketHandler handler) {
		PacketListener listener = new PacketListener(type, handler);

		listeners.add(listener);

		return listener;
	}

	/**
	 * Subscribe a listener of the specified arguments
	 *
	 * @param type Class<? extends Packet>
	 * @param handler Listener
	 * @return ServerPacketListener
	 */
	public PacketListener subscribeRespondable(Class<? extends Packet> type, PacketResponseHandler handler) {
		PacketListener listener = new PacketListener(type, handler);

		listeners.add(listener);

		return listener;
	}

	/**
	 * Unsubscribe a listener
	 *
	 * @param listener ServerPacketListener
	 */
	public void unsubscribe(PacketListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Trigger bound listeners listening to the given packet
	 *
	 * @param packet Packet
	 */
	public void triggerListeners(Packet packet, PacketConnection origin) {
		// Get the packet type
		Class<? extends Packet> type = packet.getClass();

		// Call listeners
		for(PacketListener listener : listeners) {
			if(listener.getSubscribed().contains(type)) {
				PacketHandlerInterface handler = listener.getHandler();

				if(handler instanceof PacketResponseHandler) {
					// Validate - check if the inbound packet is
					// actually a respondable packet
					// if not, this means
					if(!(packet instanceof RespondablePacket)) {
						throw new NioNetworkException("Could not handle response: Inbound packet"
							+ "is not a respondable packet");
					}

					// Cast fields to their respondable counterparts
					RespondablePacket resp_packet = (RespondablePacket) packet;
					PacketResponseHandler resp_handler = (PacketResponseHandler) handler;

					// Handle the response by a separate function
					handleResponse(resp_packet, resp_handler, origin);
				} else {
					((PacketHandler) handler).receive(packet, origin);
				}

				return;
			}
		}
	}

	/**
	 * Identify a client
	 *
	 * @param conn SocketChannel
	 * @param identity Identity packet
	 */
	public void identify(PacketConnection conn, PacketSystemAuthentication identity) {
		// Get the identity stored in the packet
		String name = identity.getIdentity();

		NioUtil.nettyLog(server.logger(), "Inbound server identifying as '" + name + "'");

		// Call hook
		callHook(HookType.AUTHENTICATION, conn);

		// Check if this identity is already registered
		if(connections.containsKey(name)) {
			NioUtil.nettyLog(server.logger(), "** Disconnecting channel: Identity '" + name + "' already identified");
			conn.disconnect("Identity already connected");
			return;
		}

		// Authentication
		if(server.isAuthEnabled()) {
			char[] server_token = server.builder.token;
			char[] client_token = identity.getAuthToken();

			if(!Arrays.equals(server_token, client_token)) {
				NioUtil.nettyLog(server.logger(), "** Disconnecting channel: Access denied");

				// Call hook
				callHook(HookType.AUTHENTICATION_FAILED, conn);

				// Disconnect
				conn.disconnect("Incorrect authentication key");

				return;
			}
		}

		// Mark the packet as authenticated
		ChannelManager.identifyConnection(conn, name);

		// Move the channel
		pending.remove(conn);
		connections.put(name, conn);

		// Call hook
		callHook(HookType.AUTHENTICATION_ACCEPTED, conn);

		// Confirm
		NioUtil.nettyLog(server.logger(), "Channel successfully identified as '" + name + "'");

		PacketSystemAuthenticationSuccess packet = new PacketSystemAuthenticationSuccess();
		conn.sendPacket(packet);
	}

	/**
	 * Handle a respondable packet response
	 *
	 * @param packet RespondablePacket
	 * @param handler Handler
	 */
	private void handleResponse(RespondablePacket packet, PacketResponseHandler handler, PacketConnection origin) {
		// Generate a response
		handler.receive(packet, origin);

		// Mark the packet as a response
		packet.markAsResponse();

		// Send
		origin.sendPacket(packet);

	}

	@Override
	public void setupChannel(SocketChannel channel) {
		// Create a new PacketConnection
		PacketConnection connection = new PacketConnection(channel);

		// Store the connection
		pending.add(connection);

		channel.eventLoop().schedule(() -> {
			// Try to remove the pending server
			if(pending.contains(connection)) {
				NioUtil.nettyLog(server.logger(), "Client failed to identify after 1 second: Disconnecting");

				// Disconnect
				connection.disconnect("Identification timeout");

				// Remove
				pending.remove(connection);
			}
		}, 1, TimeUnit.SECONDS);

		// Call hook
		callHook(HookType.CONNECTED, connection);
	}

	@Override
	public void channelDisconnected(SocketChannel channel) {
		PacketConnection disconnected = null;

		// FIX: #connection() doesn't work in this context
		for(PacketConnection pc : connections.values()) {
			if(pc.channel().id().equals(channel.id())) {
				disconnected = pc;
				break;
			}
		}

		// Check if we did actually find the name
		NioValidate.isNull(disconnected, "Unknown channel disconnected");

		NioUtil.nettyLog(server.logger(), "Channel '" + disconnected.getName() + "' disconnected");

		// Remove
		if(disconnected.isAuthenticated()) {
			connections.remove(disconnected.getName());
		} else {
			pending.remove(disconnected);
		}

		// Call hook
		callHook(HookType.DISCONNECTED, disconnected);
	}

	/**
	 * Returns the PacketConnection with the specified name
	 *
	 * @param name Name
	 * @return SocketChannel
	 */
	public PacketConnection connection(String name) {
		return connections.get(name);
	}

	/**
	 * Get a PacketConnection by it's wrapped SocketChannel
	 *
	 * @param channel SocketChannel
	 * @return PacketConnection
	 */
	public PacketConnection connection(Channel channel) {
		for(Map.Entry<String, PacketConnection> chan : connections.entrySet()) {
			if(chan.getValue().equals(channel)) {
				return chan.getValue();
			}
		}

		for(PacketConnection conn : pending) {
			if(conn.channel().equals(channel)) {
				return conn;
			}
		}

		return null;
	}

	/**
	 * Broadcast a packet
	 *
	 * @param packet Packet to broadcast
	 */
	public void broadcast(Packet packet) {
		for(PacketConnection conn : connections.values()) {
			conn.sendPacket(packet);
		}
	}

	/**
	 * Returns true when the connection exists
	 *
	 * @param name Connection name
	 * @return boolean
	 */
	public boolean connectionExists(String name) {
		return connections.get(name) != null;
	}

	/**
	 * Returns true when the connection exists
	 *
	 * @param channel Connection channel
	 * @return boolean
	 */
	public boolean connectionExists(Channel channel) {
		return connection(channel) != null;
	}

}
