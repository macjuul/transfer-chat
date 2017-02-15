package net.exodiusmc.platformer.shared.nio.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.exodiusmc.platformer.shared.nio.*;
import net.exodiusmc.platformer.shared.nio.exception.NioNetworkException;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandler;
import net.exodiusmc.platformer.shared.nio.handler.PacketHandlerInterface;
import net.exodiusmc.platformer.shared.nio.handler.PacketResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The NetworkClient class extends {@link NetworkInstance (Logger)}, and is used to handle
 * client-side Netty connections. In the case of Exodius, the NetworkClient is mostly
 * run on the Bukkit servers and the Control Panels.
 * <br>
 * To setup a new instance of the NetworkClient, initialize a new Object, then call the
 * {@link #start()} method on it. This will attempt to connect the NetworkClient to
 * the supplied ip + port.
 * <br>
 * The sent identity is used to identificate ourselves on the NetworkServer.
 *
 * @see ChannelManager
 * @author Macjuul
 * @version 2.0.0
 * @since 15-09-2016
 */
public class NetworkClient extends NetworkInstance {

	public static final int RETRY_DELAY = 5;

	public EventLoopGroup group;

	protected NetworkClientBuilder builder;
	protected boolean identified;

	private PacketConnection connection;
	private List<PacketListener> listeners;
	private ClientChannelManager manager;
	private Bootstrap bootstrap;

	/**
	 * Create a new Netty NetworkClient with the supplied logger
	 *
	 * @param builder builder used
	 */
	protected NetworkClient(NetworkClientBuilder builder) {
		super("NetworkClient", builder.logger, builder.known_packets, builder.hooks);

		this.listeners = new ArrayList<>();
		this.builder = builder;

		// Check for IdentitySuccess
		subscribe(PacketSystemAuthenticationSuccess.class, (packet, conn) -> {
			NioUtil.nettyLog(logger(), "Successfully conected to NetworkServer as '" + builder.identity + "'");

			// Mark connection as authenticated
			ChannelManager.identifyConnection(connection, builder.identity);

			// Call hook
			callHook(HookType.AUTHENTICATION_ACCEPTED, connection);
		});
	}

	@Override
	protected void initialize() {
		// Create the EventLoop groups
		group = new NioEventLoopGroup();

		// Create and configure the Client bootstrap
		// and create a new ServerChannelManager
		bootstrap = new Bootstrap();
		manager = new ClientChannelManager(this);

		bootstrap.group(group)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(manager);

		// Connect to the server
		connect();
	}

	@Override
	protected void onShutdown() {
		// Call hook
		callHook(HookType.SHUTDOWN, connection());

		// Disconnect
		disconnect();

		// Unset the ChannelManager
		manager = null;

		group.shutdownGracefully();
	}

	@Override
	protected void setupConnection(PacketConnection connection) {
		// Disconnect handling
		subscribe(PacketSystemDisconnect.class, (_packet, con) -> {
			PacketSystemDisconnect packet = (PacketSystemDisconnect) _packet;

			DisconnectReason reason = packet.getReason();

			if(reason == DisconnectReason.CUSTOM) {
				NioUtil.nettyLog(logger(), "** Channel " + con +
						" disconnected: " + packet.getDetailed() + " **");
			} else {
				NioUtil.nettyLog(logger(), "** Channel " + con +
						" disconnected: " + packet.getReason().getMessage() + " **");
			}
		});
	}

	@Override
	protected void disconnectConnection(PacketConnection disconnected) {
		// Call hook
		callHook(HookType.DISCONNECTED, connection);

		// Nullify the connection
		connection = null;

		// Reconnect
		if(builder.reconnect) reconnect(disconnected.channel());
	}


	/**
	 * Attempts to connect to the remote DataType Server. If the connection or identification fails,
	 * the DataType Client will keep trying to reconnect.
	 */
	protected void connect() {
		NioUtil.nettyLog(logger(), "Attempting to connect to NetworkServer...");

		// Attempt to bind to the port
		bootstrap.connect(builder.ip, builder.port).addListener((ChannelFuture future) -> {
			if(future.isSuccess()) {
				NioUtil.nettyLog(logger(), "NetworkClient is now successfully connected on port " + builder.port);
				NioUtil.nettyLog(logger(), "Sending client identity... (Using auth: " + (builder.token == null ? "No" : "Yes") + ")");

				// Call hook
				callHook(HookType.CONNECTED, connection());

				// Mark the Netty instance as active
				setActive(true);

				// Identify & Authenticate
				NioUtil.nettyLog(logger(), "Authenticating...");

				PacketSystemAuthentication identity = new PacketSystemAuthentication(
					builder.identity,
					builder.token
				);

				// Call hook
				callHook(HookType.AUTHENTICATION, connection());

				// Send the packet
				try {
					connection().sendPacket(identity);
				} catch(Exception ex) {
					NioUtil.nettyLog(logger(), "Failed to send Identity & Authentication packet!", ex);

					// Call hook
					callHook(HookType.AUTHENTICATION_FAILED, connection());

					stop();
				}
			} else {
				NioUtil.nettyLog(logger(), "** FAILED TO CONNECT **");
			}
		});
	}

	/**
	 * Reconnect to the server after the set amount of delay
	 */
	protected void reconnect(Channel channel) {
		// Call hook
		callHook(HookType.RECONNECT, connection());

		NioUtil.nettyLog(logger(), "Reconnecting in " + RETRY_DELAY + " seconds...");

		channel.eventLoop().schedule(this::connect, RETRY_DELAY, TimeUnit.SECONDS);
	}

	/**
	 * Disconnect from the remote server
	 */
	public void disconnect() {
		if(connectionExists())
			connection().disconnect();
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

	/**
	 * Returns true when the NetworkClient is identitied
	 *
	 * @return Boolean
	 */
	public boolean isIdentified() {
		return identified;
	}

	/**
	 * Returns the ChannelManager associated with this NetworkClient
	 *
	 * @return ServerChannelManager
	 */
	public ClientChannelManager channelManager() {
		return manager;
	}

	/**
	 * Create a new NetworkClientBuilder
	 *
	 * @return NetworkClientBuilder
	 */
	public static NetworkClientBuilder setup(String ip, int port, String identity) {
		return new NetworkClientBuilder(ip, port, identity);
	}

}
