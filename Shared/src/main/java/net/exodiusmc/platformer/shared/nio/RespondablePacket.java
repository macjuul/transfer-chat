package net.exodiusmc.platformer.shared.nio;

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
	private boolean is_response = false;
	private byte resp_id;

	public RespondablePacket() {

	}

	/**
	 * Returns the Request Tracker
	 *
	 * @return RespondableTracker
	 */
	public abstract RespondableTracker getRequest();

	/**
	 * Returns the Response Tracker
	 *
	 * @return RespondableTracker
	 */
	public abstract RespondableTracker getResponse();

	/**
	 * Set the is_response handler
	 *
	 * @param response_handler Response Handler
	 */
	public RespondablePacket setResponseHandler(Consumer<Packet> response_handler) {
		this.response_handler = response_handler;
		return this;
	}

	/**
	 * Returns the is_response handler
	 *
	 * @return Consumah
	 */
	public Consumer<Packet> getResponseHandler() {
		return response_handler;
	}

	/**
	 * Marks this respondable packet as a is_response
	 */
	public void markAsResponse() {
		this.is_response = true;
	}

	/**
	 * Returns true when this Respondable Packet is a is_response
	 *
	 * @return boolean
	 */
	public boolean IsResponse() {
		return is_response;
	}

	/**
	 * Set the is_response id
	 *
	 * @param id id
	 */
	public void setResponseId(byte id) {
		this.resp_id = id;
	}

	/**
	 * Returns the is_response id of this packet
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

	@Override
	public final void encodePayload(ByteBuf buffer) {
		if(is_response) {
			getResponse().encodePayload(buffer);
		} else {
			getRequest().encodePayload(buffer);
		}
	}

	@Override
	public final void decodePayload(ByteBuf buffer) {
		if(is_response) {
			getResponse().decodePayload(buffer);
		} else {
			getRequest().decodePayload(buffer);
		}
	}

	/**
	 * Store a is_response handler
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
