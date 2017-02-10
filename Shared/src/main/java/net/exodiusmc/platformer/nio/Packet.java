package net.exodiusmc.platformer.nio;

import io.netty.buffer.ByteBuf;

/**
 * Base class for all packet
 *
 * @author Macjuul
 * @version 3.0.0
 * @since 12-1-2017
 */
public abstract class Packet {

	public Packet() {}

	/**
	 * Encode the payload to bytes
	 *
	 * @param buffer Buffer
	 */
	public abstract void encodePayload(ByteBuf buffer);

	/**
	 * Decode the payload from bytes
	 *
	 * @param buffer Buffer
	 */
	public abstract void decodePayload(ByteBuf buffer);

	/**
	 * Overridable packet. Tells the PacketEncoder if this packet may
	 * be sent or not.
	 *
	 * @return SendRule
	 */
	public SendRule packetSendRule() {
		return SendRule.BOTH;
	}

	/**
	 * Create a new packet object from the specified type
	 *
	 * @param pclass Packet
	 * @return Packet
	 */
	public static Packet create(Class<? extends Packet> pclass) {
		try {
			return pclass == null ? null : pclass.newInstance();
		} catch (Exception exception) {
			String name = pclass.getClass().getSimpleName();

			NioUtil.nettyLog("Couldn't create packet " + name);
			NioUtil.nettyLog("Did you make sure the " + name
				+ " packet allows direct constructing?");
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Specified which source can send a specific packet
	 */
	public enum SendRule {

		CLIENT, SERVER, BOTH

	}

}
