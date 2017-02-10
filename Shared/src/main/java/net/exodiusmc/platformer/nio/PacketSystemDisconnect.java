package net.exodiusmc.platformer.nio;

import io.netty.buffer.ByteBuf;

/**
 * The disconnect packet is send just before the connection is terminated. This will
 * notify the NetworkInstance of the disconnect reason.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 28-1-2017
 */
public class PacketSystemDisconnect extends Packet {

	private String msg;

	public PacketSystemDisconnect() {}

	protected PacketSystemDisconnect(String msg) {
		this.msg = msg;
	}

	public String getDisconnectMessage() {
		return msg;
	}

	@Override
	public void encodePayload(ByteBuf buffer) {
		// Var[String] msg
		NioUtil.writeString(buffer, msg);
	}

	@Override
	public void decodePayload(ByteBuf buffer) {
		// Var[String] msg
		this.msg = NioUtil.readString(buffer);
	}
}
