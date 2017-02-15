package net.exodiusmc.platformer.shared.nio.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.exodiusmc.platformer.shared.nio.*;
import net.exodiusmc.platformer.shared.nio.exception.NioNetworkException;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandler;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandlerInterface;
import net.exodiusmc.platformer.shared.nio.handler.PacketResponseHandler;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The NetworkServer class extends {@link NetworkInstance (Logger)}, and is used to handle
 * server-side Netty connections. In the case of Exodius, the NetworkServer is run on
 * the BungeeCord server.
 * <br>
 * To setup a new instance of the NetworkServer, initialize a new Object, then call the
 * {@link #start()} method on it. This will attempt to bind the NetworkServer to the given port
 *
 * @see ChannelManager
 * @author Macjuul
 * @version 2.0.0
 * @since 15-09-2016
 */
public class NetworkServer extends NetworkInstance {
	
	public static final int IDENTITY_TIMEOUT = 20 ;
	public static final int FIXED_MAX_PENDING_CONNECTIONS = 25;
	public static final int DEFAULT_NETTY_PORT = 25560;

	protected NetworkServerBuilder builder;

	private ServerChannelManager manager;
	private EventLoopGroup boss;
	private EventLoopGroup worker;
	private List<PacketListener> listeners;
	private List<PacketConnection> pending;
	protected Map<String, PacketConnection> connections;


	/**
	 * Create a new Netty NetworkServer with the supplied logger
	 *
	 * @param builder builder used
	 */
	NetworkServer(NetworkServerBuilder builder) {
		super("NetworkServer", builder.logger, builder.known_packets, builder.hooks);

		this.builder = builder;
		this.listeners = new ArrayList<>();
		this.connections = new HashMap<>();
		this.pending = new ArrayList<>();
	}

	/**
	 * Starts the DataType Server if it is not already running. If the DataType Server is already
	 * running, the method call will simply fail, and a warning will be printed out
	 * to the logs.
	 */
	@Override
	public void initialize() {
		// Create the EventLoop groups
		boss = new NioEventLoopGroup();
		worker = new NioEventLoopGroup();
		
		// Create and configure the Server bootstrap
		ServerBootstrap bootstrap = new ServerBootstrap();
		manager = new ServerChannelManager(this);
		
		bootstrap.group(boss, worker)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, FIXED_MAX_PENDING_CONNECTIONS)
			.option(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childHandler(manager);
		
		// Attempt to bind to the port
		bootstrap.bind(builder.port).addListener((ChannelFuture future) -> {
			if(future.isSuccess()) {
				NioUtil.nettyLog(logger(), "NetworkServer is now successfully listening on port " + builder.port + " (Using auth: " + (builder.token == null ? "No" : "Yes") + ")");

				// Mark the Netty instance as active
				setActive(true);
			} else {
				NioUtil.nettyLog(logger(), "** FAILED TO START THE NETWORK SERVER **");
				NioUtil.nettyLog(logger(), "** NetworkServer failed to bind to port " + builder.port + " **");

				// We couldn't bind, so shutdown
				stop();
			}
		});
	}

	@Override
	public void onShutdown() {
		// Call hook
		callHook(HookType.SHUTDOWN, null);

		// Unset the ChannelManager
		manager = null;

		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}

	@Override
	protected void setupConnection(PacketConnection connection) {
		// Store the connection
		pending.add(connection);

		connection.channel().eventLoop().schedule(() -> {
			// Try to remove the pending server
			if(pending.contains(connection)) {
				NioUtil.nettyLog(logger(), "Client failed to identify after 1 second: Disconnecting");

				// Disconnect
				connection.disconnect(DisconnectReason.AUTH_TIMEOUT);

				// Remove
				pending.remove(connection);
			}
		}, 1, TimeUnit.SECONDS);

		// Call hook
		callHook(HookType.CONNECTED, connection);
	}

	@Override
	protected void disconnectConnection(PacketConnection disconnected) {
		// Check if we did actually find the name
		NioValidate.isNull(disconnected, "Unknown channel disconnected");

		NioUtil.nettyLog(logger(), "Channel '" + disconnected.getName() + "' disconnected");

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

		NioUtil.nettyLog(logger(), "Inbound server identifying as '" + name + "'");

		// Call hook
		callHook(HookType.AUTHENTICATION, conn);

		// Check if this identity is already registered
		if(connections.containsKey(name)) {
			NioUtil.nettyLog(logger(), "** Disconnecting channel: Identity '" + name + "' already identified");
			conn.disconnect(DisconnectReason.IDENTITY_TAKEN);
			return;
		}

		// Authentication
		if(isAuthEnabled()) {
			char[] server_token = builder.token;
			char[] client_token = identity.getAuthToken();

			if(!Arrays.equals(server_token, client_token)) {
				NioUtil.nettyLog(logger(), "** Disconnecting channel: Access denied");

				// Call hook
				callHook(HookType.AUTHENTICATION_FAILED, conn);

				// Disconnect
				conn.disconnect(DisconnectReason.AUTH_KEY_WRONG);

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
		NioUtil.nettyLog(logger(), "Channel successfully identified as '" + name + "'");

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

	/**
	 * Returns true when authentication is enabled
	 *
	 * @return boolean
	 */
	public boolean isAuthEnabled() {
		return builder.token != null;
	}

	/**
	 * Returns the ChannelManager associated with this NetworkClient
	 *
	 * @return ServerChannelManager
	 */
	public ServerChannelManager channelManager() {
		return manager;
	}

	/**
	 * Create a new NetworkClientBuilder
	 *
	 * @return NetworkClientBuilder
	 */
	public static NetworkServerBuilder setup(int port) {
		return new NetworkServerBuilder(port);
	}
}
