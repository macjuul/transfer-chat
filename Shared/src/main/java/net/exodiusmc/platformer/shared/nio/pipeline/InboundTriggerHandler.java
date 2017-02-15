package net.exodiusmc.platformer.shared.nio.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.exodiusmc.platformer.shared.nio.*;
import net.exodiusmc.platformer.shared.nio.client.NetworkClient;
import net.exodiusmc.platformer.shared.nio.server.NetworkServer;

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

	private NetworkInstance net_instance;

	public InboundTriggerHandler(NetworkInstance net_instance) {
		this.net_instance = net_instance;
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
		if(net_instance instanceof NetworkClient) {
			// Trigger - client
			((NetworkClient) net_instance).triggerListeners(packet);
		} else if(net_instance instanceof NetworkServer) {
			NetworkServer server = (NetworkServer) net_instance;

			// Get the connection from the current channel
			PacketConnection origin = server.connection(ctx.channel());

			// Identity - Validate incoming identity packet
			if(packet instanceof PacketSystemAuthentication) {
				//noinspection ConstantConditions
				server.identify(origin, (PacketSystemAuthentication) packet);
				return;
			} else if(!origin.isAuthenticated()) {
				// When the client is not authenticated, and tried to send
				// a non-auth packet, ignore and log
				NioUtil.nettyLog(net_instance.logger(), "[WARNING] Received non-auth packet from unauthenticated server!");
				return;
			}

			// Triger - server
			server.triggerListeners(packet, origin);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		Logger log = net_instance.logger();

		// Log the exception
		NioUtil.nettyLog(log, "[PACKET] [SEVERE] Exception occurred during PacketDecoding (Trigger) " + e.getCause().getMessage()
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
