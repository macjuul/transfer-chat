package net.exodiusmc.platformer.shared.nio.client;

import io.netty.channel.socket.SocketChannel;
import net.exodiusmc.platformer.shared.nio.ChannelManager;
import net.exodiusmc.platformer.shared.nio.PacketConnection;

/**
 * Clientside implementation of the ChannelManager
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public class ClientChannelManager extends ChannelManager {

	private NetworkClient client;

	/**
	 * Create a new ChannelManager for the given NetworkInstance
	 *
	 * @param parent NetworkInstance
	 */
	ClientChannelManager(NetworkClient parent) {
		super(parent);

		this.client = parent;
	}

	@Override
	public void setupChannel(SocketChannel channel) {
		// Wrap the SocketChannel in a PacketConnection
		PacketConnection conn = new PacketConnection(channel);

		client.setupConnection(conn);
	}

	@Override
	public void channelDisconnected(SocketChannel channel) {
		client.disconnectConnection(client.connection());
	}

}
