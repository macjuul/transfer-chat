package net.exodiusmc.platformer.nio;

import io.netty.buffer.ByteBuf;

/**
 * The identification confirm packet is a special packet used when connecting
 * a client to a server. The identification confirm packet will inform the
 * client that it's supplied identity is valid, and furtherpackets can be sent.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class PacketSystemAuthenticationSuccess extends Packet {

	public PacketSystemAuthenticationSuccess() {}

	@Override
	public void encodePayload(ByteBuf buffer) {
	}

	@Override
	public void decodePayload(ByteBuf buffer) {
	}
}
