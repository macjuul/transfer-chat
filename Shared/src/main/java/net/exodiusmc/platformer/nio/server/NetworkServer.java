package net.exodiusmc.platformer.nio.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.exodiusmc.platformer.nio.ChannelManager;
import net.exodiusmc.platformer.nio.HookType;
import net.exodiusmc.platformer.nio.NetworkInstance;
import net.exodiusmc.platformer.nio.NioUtil;

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

	/**
	 * Create a new Netty NetworkServer with the supplied logger
	 *
	 * @param builder builder used
	 */
	protected NetworkServer(NetworkServerBuilder builder) {
		super("NetworkServer", builder.logger);

		this.builder = builder;
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
				NioUtil.nettyLog(logger(), "** FAILED TO START THE DATASERVER **");
				NioUtil.nettyLog(logger(), "** NetworkServer failed to bind to port " + builder.port + " **");

				// We couldn't bind, so shutdown
				stop();
			}
		});
	}

	@Override
	public void onShutdown() {
		// Call hook
		manager.callHook(HookType.SHUTDOWN, null);

		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}

	/**
	 * Returns true when authentication is enabled
	 *
	 * @return boolean
	 */
	public boolean authEnabled() {
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
