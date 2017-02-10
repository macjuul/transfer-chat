package net.exodiusmc.platformer.nio.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.exodiusmc.platformer.nio.*;
import net.exodiusmc.platformer.nio.client.ClientChannelManager;
import net.exodiusmc.platformer.nio.server.ServerChannelManager;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Netty channel handler that triggers subscribed listeners
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 13-1-2017
 */
public class InboundTriggerHandler extends ChannelInboundHandlerAdapter {

	private ChannelManager manager;

	public InboundTriggerHandler(ChannelManager manager) {
		this.manager = manager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Packet packet = (Packet) msg;

		// RESPONDABLE PACKETS
		if(packet instanceof RespondablePacket) {
			RespondablePacket resp_packet = (RespondablePacket) packet;

			// Only response handlers will be handled by this code. The
			// actual respondable payload handlers are handled
			// seperately in the ChannelManager classes.
			if(resp_packet.isResponse()) {
				handleResponse(resp_packet);
				return;
			}
		}

		// NORMAL PACKETS
		if(manager instanceof ClientChannelManager) {
			// Trigger - client
			((ClientChannelManager) manager).triggerListeners(packet);
		} else if(manager instanceof ServerChannelManager) {
			ServerChannelManager mngr = (ServerChannelManager) manager;

			// Get the connection from the current channel
			PacketConnection origin = mngr.connection(ctx.channel());

			// Identity - Validate incoming identity packet
			if(packet instanceof PacketSystemAuthentication) {
				//noinspection ConstantConditions
				mngr.identify(origin, (PacketSystemAuthentication) packet);
				return;
			} else if(!origin.isAuthenticated()) {
				// When the client is not authenticated, and tried to send
				// a non-auth packet, ignore and log
				NioUtil.nettyLog(manager.getParent().logger(), "[WARNING] Received non-auth packet from unauthenticated server!");
				return;
			}

			// Triger - server
			mngr.triggerListeners(packet, origin);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		Logger log = manager.getParent().logger();

		// Log the exception
		NioUtil.nettyLog(log, "[PACKET] [SEVERE] Exception corrured during PacketDecoding (Trigger) " + e.getCause().getMessage()
			+ ". Closing channel " + ctx.channel().id());
		e.printStackTrace();
	}

	/**
	 * Handle the calling of a responded packet
	 *
	 * @param packet RespondablePacket
	 */
	private void handleResponse(RespondablePacket packet) {
		Consumer<Packet> handler = RespondablePacket.retrieveHandler(packet.getResponseId());

		if(handler != null) {
			handler.accept(packet);
		} else {
			NioUtil.nettyLog("** RECEIVED EXPIRED/UNRESOLVED PACKET RESPONSE **");
		}
	}

}
