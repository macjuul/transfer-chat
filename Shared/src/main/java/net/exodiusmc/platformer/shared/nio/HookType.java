package net.exodiusmc.platformer.shared.nio;

/**
 * Possible hooks that can be listened to.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 27-1-2017
 */
public enum HookType {

	/**
	 * Called when the NetworkInstance has connected to a new channel.
	 */
	CONNECTED,

	/**
	 * Called when the connection with a channel has been broken
	 */
	DISCONNECTED,

	/**
	 * <b>Client-only</b><br>
	 * Called when the NetworkClient attempts to reconnect
	 */
	RECONNECT,

	/**
	 * Called when the NetworkInstance shuts down. PacketConnection will be null
	 */
	SHUTDOWN,

	/**
	 * Called when the NetworkClient sends its identity or when
	 * the NetworkServer authenticates a new channel
	 */
	AUTHENTICATION,

	/**
	 * Called when either the NetworkServer accepts an
	 * identification, or when the NetworkClient receives
	 * the result.
	 */
	AUTHENTICATION_ACCEPTED,

	/**
	 * Called when either the NetworkServer declines an
	 * identification, or when the NetworkClient receives
	 * the result.
	 */
	AUTHENTICATION_FAILED

}
