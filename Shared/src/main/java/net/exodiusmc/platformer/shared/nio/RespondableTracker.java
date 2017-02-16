package net.exodiusmc.platformer.shared.nio;

import io.netty.buffer.ByteBuf;

/**
 * Represents a payload tracker for either the request and response
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 15/02/2017
 */
public interface RespondableTracker {

	/**
	 * Encode the payload to bytes
	 *
	 * @param buffer Buffer
	 */
	void encodePayload(ByteBuf buffer);

	/**
	 * Decode the payload from bytes
	 *
	 * @param buffer Buffer
	 */
	void decodePayload(ByteBuf buffer);

}
