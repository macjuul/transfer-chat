package net.exodiusmc.platformer.shared.nio;

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

	private DisconnectReason reason;
	private String detailed;

	public PacketSystemDisconnect() {}

	protected PacketSystemDisconnect(DisconnectReason reason) {
		this.reason = reason;
	}

	protected PacketSystemDisconnect(String reason) {
		this.reason = DisconnectReason.CUSTOM;
		this.detailed = reason;
	}

	/**
	 * Returns the reason of the disconnect
	 *
	 * @return DisconnectReason
	 */
	public DisconnectReason getReason() {
		return reason;
	}

	/**
	 * Returns a more detailed reason to disconnect when the
	 * reason is CUSTOM
	 *
	 * @return Detailed disconnect reason
	 */
	public String getDetailed() {
		return detailed;
	}

	/**
	 * Apply the disconnect details to a connection
	 *
	 * @param connection PacketConnection to apply to
	 */
	public void apply(PacketConnection connection) {
		connection.disconnect_reason = reason;
		connection.detailed_reason = detailed;
	}

	@Override
	public void encodePayload(ByteBuf buffer) {
		// Byte - reason
		buffer.writeByte(reason.ordinal());

		if(reason == DisconnectReason.CUSTOM) {
			NioUtil.writeString(buffer, detailed);
		}
	}

	@Override
	public void decodePayload(ByteBuf buffer) {
		// Byte - reason
		this.reason = DisconnectReason.values()[buffer.readByte()];

		if(reason == DisconnectReason.CUSTOM) {
			this.detailed = NioUtil.readString(buffer);
		}
	}
}
