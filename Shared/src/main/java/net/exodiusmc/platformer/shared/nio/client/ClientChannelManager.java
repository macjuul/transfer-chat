package net.exodiusmc.platformer.shared.nio.client;

import io.netty.channel.socket.SocketChannel;
import net.exodiusmc.platformer.shared.nio.*;
import net.exodiusmc.platformer.shared.nio.exception.NioNetworkException;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandler;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandlerInterface;
import net.exodiusmc.platformer.shared.nio.handler.PacketResponseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Clientside implementation of the ChannelManager
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class ClientChannelManager extends ChannelManager {

	private List<PacketListener> listeners;
	private NetworkClient client;
	private PacketConnection connection;

	/**
	 * Create a new ChannelManager for the given NetworkInstance
	 *
	 * @param parent NetworkInstance
	 */
	ClientChannelManager(NetworkClient parent) {
		super(parent, parent.builder.hooks, parent.builder.known_packets);

		this.listeners = new ArrayList<>();
		this.client = parent;

		// Check for IdentitySuccess
		subscribe(PacketSystemAuthenticationSuccess.class, (packet, conn) -> {
			NioUtil.nettyLog(parent.logger(), "Successfully conected to NetworkServer as '" + parent.builder.identity + "'");

			// Mark connection as authenticated
			ChannelManager.identifyConnection(connection, parent.builder.identity);

			// Call hook
			callHook(HookType.AUTHENTICATION_ACCEPTED, connection);
		});
	}

	@Override
	public void setupChannel(SocketChannel channel) {
		// Wrap the SocketChannel in a PacketConnection
		this.connection = new PacketConnection(channel);

		// Disconnect handling
		subscribe(PacketSystemDisconnect.class, (_packet, con) -> {
			PacketSystemDisconnect packet = (PacketSystemDisconnect) _packet;

			NioUtil.nettyLog(client.logger(), "** Channel " + con.channel() +
				" disconnected: " + packet.getDisconnectMessage());
		});

		// Call hook
		callHook(HookType.CONNECTED, connection);
	}

	@Override
	public void channelDisconnected(SocketChannel channel) {
		// Call hook
		callHook(HookType.DISCONNECTED, connection);

		// Nullify the current channel
		this.connection = null;

		// Reconnect
		if(client.builder.reconnect) client.reconnect(channel);
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
	public void triggerListeners(Packet packet) {
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

					// Handle the response by a seperate function
					handleResponse(resp_packet, resp_handler);

					return;
				}

				((PacketHandler) handler).receive(packet, connection);

				return;
			}
		}
	}

	/**
	 * Handle a respondable packet response
	 *
	 * @param packet RespondablePacket
	 * @param handler Handler
	 */
	private void handleResponse(RespondablePacket packet, PacketResponseHandler handler) {
		// Generate a response
		handler.receive(packet, connection);

		// Mark the packet as a response
		packet.markAsResponse();

		// Send
		connection.sendPacket(packet);
	}

	/**
	 * Returns the PacketConnection that is currently active
	 *
	 * @return PacketConnection
	 */
	public PacketConnection connection() {
		return connection;
	}

	/**
	 * Returns true when the connection exists
	 *
	 * @return boolean
	 */
	public boolean connectionExists() {
		return connection != null;
	}

}
