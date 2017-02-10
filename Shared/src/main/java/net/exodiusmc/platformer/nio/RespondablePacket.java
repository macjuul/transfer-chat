package net.exodiusmc.platformer.nio;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class Description
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 19-1-2017
 */
public abstract class RespondablePacket extends Packet {

	public static byte RESPONSE_ID = 0;
	private static Map<Byte, Consumer<Packet>> handlers = new HashMap<>();

	private Consumer<Packet> response_handler;
	private boolean response = false;
	private byte resp_id;

	public RespondablePacket() {}

	/**
	 * Encode the response to bytes
	 *
	 * @param buffer Buffer
	 */
	public abstract void encodeResponse(ByteBuf buffer);

	/**
	 * Decode the response from bytes
	 *
	 * @param buffer Buffer
	 */
	public abstract void decodeResponse(ByteBuf buffer);

	/**
	 * Set the response handler
	 *
	 * @param response_handler Response Handler
	 */
	public RespondablePacket setResponseHandler(Consumer<Packet> response_handler) {
		this.response_handler = response_handler;
		return this;
	}

	/**
	 * Returns the response handler
	 *
	 * @return Consumah
	 */
	public Consumer<Packet> getResponseHandler() {
		return response_handler;
	}

	/**
	 * Marks this respondable packet as a response
	 */
	public void markAsResponse() {
		this.response = true;
	}

	/**
	 * Returns true when this Respondable Packet is a response
	 *
	 * @return boolean
	 */
	public boolean isResponse() {
		return response;
	}

	/**
	 * Set the response id
	 *
	 * @param id id
	 */
	public void setResponseId(byte id) {
		this.resp_id = id;
	}

	/**
	 * Returns the response id of this packet
	 *
	 * @return Response ID
	 */
	public byte getResponseId() {
		return resp_id;
	}

	/**
	 * packetSendRule final override. RespondablePackets will have
	 * to be sent from both sides.
	 *
	 * @return SendRule.BOTH
	 */
	@Override
	public final SendRule packetSendRule() {
		return SendRule.BOTH;
	}

	/**
	 * Store a response handler
	 *
	 * @param id Id
	 * @param handler Packet handler
	 */
	public static void storeHandler(byte id, Consumer<Packet> handler) {
		handlers.put(id, handler);
	}

	/**
	 * Retrieve the stored handler
	 *
	 * @param id Id
	 * @return Packet handler
	 */
	public static Consumer<Packet> retrieveHandler(byte id) {
		return handlers.get(id);
	}
}
