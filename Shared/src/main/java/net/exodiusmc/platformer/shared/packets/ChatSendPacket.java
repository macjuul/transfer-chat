package net.exodiusmc.platformer.shared.packets;

import io.netty.buffer.ByteBuf;
import net.exodiusmc.platformer.shared.nio.NioUtil;
import net.exodiusmc.platformer.shared.nio.RespondablePacket;
import net.exodiusmc.platformer.shared.nio.RespondableTracker;

/**
 * Send a chat packet to the server. A response will be
 * sent back containing the message id. this id is supposed to
 * be stored alongside the message.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 10/02/2017
 */
public class ChatSendPacket extends RespondablePacket {

    // Request
    private String msg;
    private byte tid;

    // Response
    private byte mid;

    public ChatSendPacket() {}

	@Override
	public RespondableTracker getRequest() {
		return null;
	}

	@Override
	public RespondableTracker getResponse() {
		return null;
	}

	public ChatSendPacket(String msg) {
        this.msg = msg;
    }

    public void encodeResponse(ByteBuf buffer) {

    }

    public void decodeResponse(ByteBuf buffer) {

    } 

    public void encodePayloadZ(ByteBuf buffer) {
        // msg
        NioUtil.writeString(buffer, msg);
    }

    public void decodePayloadZ(ByteBuf buffer) {
        // msg
        this.msg = NioUtil.readString(buffer);
    }
}
