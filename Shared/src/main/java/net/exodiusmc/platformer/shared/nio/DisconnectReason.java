package net.exodiusmc.platformer.shared.nio;

/**
 * An enum containing the possible reasons of a lost
 * connection.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 14/02/2017
 */
public enum DisconnectReason {

	NO_REASON("Connection lost"),
	IDENTITY_TAKEN("Identity already in use"),
	AUTH_KEY_WRONG("Failed to authenticate"),
	AUTH_TIMEOUT("Failed to authenticate in time"),
	CUSTOM("");

	private String reason;

	DisconnectReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Returns the disconnect reason
	 *
	 * @return String
	 */
	public String getMessage() {
		return reason;
	}
}
