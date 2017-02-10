package net.exodiusmc.platformer.shared.nio;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * The identification packet is a special packet used when connecting
 * a client to a server. The identification packet will inform the
 * server of the name the client should be refered to with.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class PacketSystemAuthentication extends Packet {

	private String name;
	private char[] token;

	public PacketSystemAuthentication() {}

	public PacketSystemAuthentication(String name, char[] token) {
		this.name = name;
		this.token = token;
	}

	/**
	 * Returns the Identity stored on this packet
	 *
	 * @return identity
	 */
	public String getIdentity() {
		return name;
	}

	/**
	 * Returns the authentication token
	 *
	 * @return Token
	 */
	public char[] getAuthToken() {
		return token;
	}

	@Override
	public void encodePayload(ByteBuf buffer) {
		// Var[String] name
		NioUtil.writeString(buffer, name);

		// Var[char[]] token
		NioUtil.writeVarBytes(buffer, new String(token).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void decodePayload(ByteBuf buffer) {
		// Var[String] name
		this.name = NioUtil.readString(buffer);

		// Var[char[]] token
		this.token = new String(NioUtil.readVarBytes(buffer), StandardCharsets.UTF_8).toCharArray();
	}
}
