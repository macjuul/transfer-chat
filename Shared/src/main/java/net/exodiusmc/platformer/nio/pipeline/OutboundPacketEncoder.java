package net.exodiusmc.platformer.nio.pipeline;

import com.google.common.collect.BiMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.exodiusmc.platformer.nio.*;
import net.exodiusmc.platformer.nio.exception.NioNetworkException;

import java.util.logging.Logger;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 13-1-2017
 */
public class OutboundPacketEncoder extends MessageToByteEncoder<Packet> {

	private ChannelManager manager;

	public OutboundPacketEncoder(ChannelManager manager) {
		this.manager = manager;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buffer) throws Exception {
		// Validate - check if packet type is null
		NioValidate.isNull(packet, "Cannot send null packet");

		// Get a ref to all packets and their ids
		BiMap<Class<? extends Packet>, Byte> packets = manager.getPackets().inverse();

		Class<? extends Packet> clazz = packet.getClass();

		// Validate - check if id is null
		if(NioValidate.isNull(packets.get(clazz))) {
			NioUtil.nettyLog("[WARNING] Attempted to send unknown packet '" + packet.getClass().getSimpleName() +
				". Make sure this packet has been registered during NetworkInstance building.");
			return;
		}

		// Get the packet id
		byte id = packets.get(clazz);

		// Write the packet type to the buffer
		buffer.writeByte(id);

		// Check if the packet is a respondable packet
		if(packet instanceof RespondablePacket) {
			RespondablePacket respondable = (RespondablePacket) packet;

			// Check if this packet is a response
			if(respondable.isResponse()) {
				// Write the response id
				buffer.writeByte(respondable.getResponseId());

				// Mark this request
				buffer.writeBoolean(true);

				// Encode the packet as response
				respondable.encodeResponse(buffer);

				NioUtil.nettyLog(manager.getParent().logger(), "[PACKET] Sending response packet (" + packet.getClass().getSimpleName() + ")");


				return; // <-- We are done encoding
			} else {
				// Write the response id
				byte resp_id = ++RespondablePacket.RESPONSE_ID;

				buffer.writeByte(resp_id);

				// Mark this request
				buffer.writeBoolean(false);

				// Store the handler
				RespondablePacket.storeHandler(resp_id, respondable.getResponseHandler());
			}
		}

		// Try to ncode the packet
		try {
			packet.encodePayload(buffer);

			NioUtil.nettyLog(manager.getParent().logger(), "[PACKET] Sending " + packet.getClass().getSimpleName() + " packet (size=" + (buffer.readableBytes() - 1) + ",id=" + id + ")");
		} catch(NullPointerException ex) {
			throw new NioNetworkException("Encountered null field whilst encoding payload for "
				+ packet.getClass().getSimpleName() + " packet. This might be caused by a packet"
				+ " being initialized with an empty constructor (This should be avoided).");
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		Logger log = manager.getParent().logger();

		try {
			// Log the exception
			NioUtil.nettyLog(log, "[PACKET] [SEVERE] Exception corrured during PacketEncoding " + e.getCause().getMessage()
				+ ". Closing channel " + ctx.channel().id());

			// Close the channel
			ctx.channel().close();
		} catch (Exception ex) {
			NioUtil.nettyLog(log, "[SEVERE] ERROR trying to close socket because we got an unhandled exception");
		}
	}

}
