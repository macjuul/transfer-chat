package net.exodiusmc.platformer.shared.nio;

import com.google.common.collect.BiMap;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for the NetworkClient and NetworkServer
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 10-1-2017
 */
public abstract class NetworkInstance {

	private BiMap<Byte, Class<? extends Packet>> packets;
	private ListMultimap<HookType, Consumer<PacketConnection>> hooks;
	private Logger logger;
	private String name;
	private boolean active;

	/**
	 * Create a new Netty NetworkInstance with the supplied logger
	 *
	 * @param name The (display) name of this Netty instance
	 * @param logger Logger
	 */
	public NetworkInstance(String name,
           Logger logger,
           BiMap<Byte, Class<? extends Packet>> packets,
           ListMultimap<HookType, Consumer<PacketConnection>> hooks) {

		// Create a logger if null
		if(logger == null) {
			logger = Logger.getLogger("PacketNetwork");
		}

		this.hooks = hooks;
		this.packets = packets;
		this.logger = logger;
		this.name = name;
	}

	/**
	 * Start the NetworkInstance and setup
	 */
	public final void start() {
		NioUtil.nettyLog(logger, "Starting " + displayName());
		long old = System.currentTimeMillis();

		// Check if already active
		if(isActive()) {
			NioUtil.nettyLog(logger, "** FAILED TO START " + displayName() + " **");
			NioUtil.nettyLog(logger, "** This NetworkInstance is already active **");
			NioUtil.nettyLog(logger, "** Please use shutdown() first before  **");
			NioUtil.nettyLog(logger, "** re-starting the DataType Instance.      **");
			return;
		}

		// Call the extended initialize() method
		initialize();

		long now = System.currentTimeMillis() - old;
		NioUtil.nettyLog(logger, "Done! (" + now + "ms) " + displayName() + " is now waiting for connections");
	}

	/**
	 * Call all registered hooks of the specified HookType
	 *
	 * @param type HookType
	 * @param connection PacketConnection
	 * @return true when at least one hook was triggered
	 */
	public boolean callHook(HookType type, PacketConnection connection) {
		List<Consumer<PacketConnection>> triggers = hooks.get(type);

		// Return if none are hooked
		if(triggers == null || triggers.size() == 0) return false;

		for(Consumer<PacketConnection> conn : triggers) {
			try {
				conn.accept(connection);
			} catch(Exception ex) {
				logger.log(Level.WARNING, "Exception caught during hook execution", ex);
			}
		}

		return true;
	}

	/**
	 * Register a new hook
	 *
	 * @param type HookType
	 * @param handler handler
	 * @return the handler
	 */
	public Consumer<PacketConnection> registerHook(HookType type, Consumer<PacketConnection> handler) {
		this.hooks.put(type, handler);
		return handler;
	}

	/**
	 * Unregister a listening hook
	 * @param type HookType
	 * @param handler handler
	 * @return true if successful
	 */
	public boolean unregisterHook(HookType type, Consumer<PacketConnection> handler) {
		return this.hooks.remove(type, handler);
	}

	/**
	 * Returns a BiMap of packets
	 *
	 * @return Packets
	 */
	public BiMap<Byte, Class<? extends Packet>> getPackets() {
		return packets;
	}

	/**
	 * Set the active state on this NetworkInstance
	 *
	 * @param active Active
	 */
	protected void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Returns true when the NetworkInstance is active
	 *
	 * @return Active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Returns the logger associated with this NetworkInstance
	 *
	 * @return Logger
	 */
	public Logger logger() {
		return logger;
	}

	/**
	 * Returns the display name given to this instance
	 *
	 * @return String
	 */
	public String displayName() {
		return name;
	}

	/**
	 * Shut down the NetworkInstance
	 */
	public final void stop() {
		if(!active) return;

		// Set the state to inactive
		setActive(false);

		// Call the extended onShutdown() method
		onShutdown();

		// Log
		NioUtil.nettyLog(logger, "Successfully stopped the " + displayName() + " Network service");
	}

	/**
	 * Initialize the NetworkInstance. This is where the Netty bootstrap should be run
	 */
	protected abstract void initialize();

	/**
	 * Shuts down the NetworkInstance
	 */
	protected abstract void onShutdown();

	/**
	 * Setup a new packet connection
	 *
	 * @param connection connection
	 */
	protected abstract void setupConnection(PacketConnection connection);

	/**
	 * Handle the closing of a connection
	 *
	 * @param disconnected connection
	 */
	protected abstract void disconnectConnection(PacketConnection disconnected);
}
