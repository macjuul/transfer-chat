package net.exodiusmc.platformer.nio;

import java.util.logging.Logger;

/**
 * Base class for the NetworkClient and NetworkServer
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 10-1-2017
 */
public abstract class NetworkInstance {

	private Logger logger;
	private String name;
	private boolean active;

	/**
	 * Create a new Netty NetworkInstance with the supplied logger
	 *
	 * @param name The (display) name of this Netty instance
	 * @param logger Logger
	 */
	public NetworkInstance(String name, Logger logger) {
		// Create a logger if null
		if(logger == null) {
			logger = Logger.getLogger("PacketNetwork");
		}

		this.logger = logger;
		this.name = name;
	}

	/**
	 * Start the NetworkInstance and setup
	 */
	public final void start() {
		NioUtil.nettyLog(logger, "Starting Netty instance '" + displayName() + "'...");

		// Check if already active
		if(isActive()) {
			NioUtil.nettyLog(logger, "** FAILED TO START NETTY " + displayName() + " **");
			NioUtil.nettyLog(logger, "** This NetworkInstance is already active **");
			NioUtil.nettyLog(logger, "** Please use shutdown() first before  **");
			NioUtil.nettyLog(logger, "** re-starting the DataType Instance.      **");
			return;
		}

		// Call the extended initialize() method
		initialize();

		NioUtil.nettyLog(logger, "Successfully enabled the " + displayName() + " Netty instance");
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
}
