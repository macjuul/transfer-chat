package net.exodiusmc.platformer.shared.nio.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.exodiusmc.platformer.shared.nio.*;

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

	private ClientChannelManager manager;
	private Bootstrap bootstrap;

	/**
	 * Create a new Netty NetworkClient with the supplied logger
	 *
	 * @param builder builder used
	 */
	protected NetworkClient(NetworkClientBuilder builder) {
		super("NetworkClient", builder.logger);

		this.builder = builder;
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
		manager.callHook(HookType.SHUTDOWN, manager.connection());

		// Disconnect
		disconnect();

		group.shutdownGracefully();
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

				// Mark the Netty instance as active
				setActive(true);

				// Identitify & Authenticate
				NioUtil.nettyLog(logger(), "Authenticating...");

				PacketSystemAuthentication identity = new PacketSystemAuthentication(
					builder.identity,
					builder.token
				);

				// Call hook
				manager.callHook(HookType.AUTHENTICATION, manager.connection());

				// Send the packet
				try {
					manager.connection().sendPacket(identity);
				} catch(Exception ex) {
					NioUtil.nettyLog(logger(), "Failed to send Identity & Authentication packet!", ex);

					// Call hook
					manager.callHook(HookType.AUTHENTICATION_FAILED, manager.connection());

					stop();
				}
			} else {
				NioUtil.nettyLog(logger(), "** Failed to connect to server");
			}
		});
	}

	/**
	 * Reconnect to the server after the set amount of delay
	 */
	protected void reconnect(Channel channel) {
		// Call hook
		manager.callHook(HookType.RECONNECT, manager.connection());

		NioUtil.nettyLog(logger(), "** Reconnecting in " + RETRY_DELAY + " seconds");

		channel.eventLoop().schedule(this::connect, RETRY_DELAY, TimeUnit.SECONDS);
	}

	/**
	 * Disconnect from the remote server
	 */
	public void disconnect() {
		manager.connection().disconnect();
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
