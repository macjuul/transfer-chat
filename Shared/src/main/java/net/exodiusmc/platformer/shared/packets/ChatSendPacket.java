package net.exodiusmc.platformer.shared.packets;

import io.netty.buffer.ByteBuf;
import net.exodiusmc.platformer.shared.nio.NioUtil;
import net.exodiusmc.platformer.shared.nio.RespondablePacket;

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

    public ChatSendPacket(String msg) {
        this.msg = msg;
    }

    @Override
    public void encodeResponse(ByteBuf buffer) {

    }

    @Override
    public void decodeResponse(ByteBuf buffer) {

    } 

    @Override
    public void encodePayload(ByteBuf buffer) {
        // msg
        NioUtil.writeString(buffer, msg);
    }

    @Override
    public void decodePayload(ByteBuf buffer) {
        // msg
        this.msg = NioUtil.readString(buffer);
    }
}
