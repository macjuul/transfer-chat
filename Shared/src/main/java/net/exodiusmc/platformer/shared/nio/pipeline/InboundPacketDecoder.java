package net.exodiusmc.platformer.shared.nio.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.exodiusmc.platformer.shared.nio.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Macjuul
 * @version 2.0.0
 * @since 13-1-2017
 */
public class InboundPacketDecoder extends ByteToMessageDecoder {

	private ChannelManager manager;

	public InboundPacketDecoder(ChannelManager manager) {
		this.manager = manager;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> list) throws Exception {

		// Discard if no bytes are found
		if(buffer.readableBytes() == 0) return;

		// Read the packet type byte
		byte id = buffer.readByte();

		NioUtil.nettyLog(manager.getParent().logger(), "[PACKET] Received packet (size=" + buffer.readableBytes() + ",id=" + id + ")");

		// Get the PacketType
		Class<? extends Packet> type = manager.getPackets().get(id);

		// Validate - check if packet type is null
		NioValidate.isNull(type, "[PACKET] Received unknown packet id '" + id +
		"'. Make sure this packet has been registered during NetworkInstance building.");

		// Create the (empty) packet
		Packet packet = Packet.create(type);

		// Validate - check if packet is null
		NioValidate.isNull(packet, "Packet is null");

		// Check if the packet is a respondable packet
		if(packet instanceof RespondablePacket) {
			byte resp_id = buffer.readByte();
			boolean response = buffer.readBoolean();

			// Cast our packet to a RespondablePacket
			RespondablePacket resp_packet = (RespondablePacket) packet;

			// Update the respondable packet meta fields
			resp_packet.setResponseId(resp_id);

			if(response) {
				resp_packet.markAsResponse();
				resp_packet.decodeResponse(buffer);
			} else {
				resp_packet.decodePayload(buffer);
			}
		} else {
			// Decode the packet
			try {
				packet.decodePayload(buffer);
			} catch(DecoderException ex) {
				NioUtil.nettyLog(manager.getParent().logger(), "[SEVERE] Failed to decode " + type.getSimpleName() + " packet", ex);
			}
		}

		// Flush the packet
		list.add(packet);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		Logger log = manager.getParent().logger();

		try {
			// Log the exception
			NioUtil.nettyLog(log, "[PA[SEVERE] Exception corrured during PacketDecoding " + e.getCause().getMessage()
				+ ". Closing channel " + ctx.channel().id());

			// Close the channel
			ctx.channel().close();
		} catch (Exception ex) {
			NioUtil.nettyLog(log, "[SEVERE] ERROR trying to close socket because we got an unhandled exception");
		}
	}
}
