package net.exodiusmc.platformer.shared.nio.server;

import io.netty.channel.socket.SocketChannel;
import net.exodiusmc.platformer.shared.nio.ChannelManager;
import net.exodiusmc.platformer.shared.nio.NioUtil;
import net.exodiusmc.platformer.shared.nio.PacketConnection;

/**
 * Serverside implementation of the ChannelManager
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class ServerChannelManager extends ChannelManager {

	private NetworkServer server;

	/**
	 * Create a new ChannelManager for the given NetworkInstance
	 *
	 * @param parent NetworkInstance
	 */
	public ServerChannelManager(NetworkServer parent) {
		super(parent);

		this.server = server;
	}

	@Override
	public void setupChannel(SocketChannel channel) {
		// Create a new PacketConnection
		PacketConnection connection = new PacketConnection(channel);

		// Store the connection
		server.setupConnection(connection);
	}

	@Override
	public void channelDisconnected(SocketChannel channel) {
		PacketConnection disconnected = null;

		// FIX: #connection() doesn't work in this context
		for(PacketConnection pc : server.connections.values()) {
			if(pc.channel().id().equals(channel.id())) {
				disconnected = pc;
				break;
			}
		}

		// Check if we did actually find the name
		NioUtil.isNull(disconnected, "Unknown channel disconnected");

		server.disconnectConnection(disconnected);
	}

}
