package net.exodiusmc.platformer.shared.nio;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import net.exodiusmc.platformer.shared.nio.exception.NioNetworkException;

/**
 * Represents a connection between a NetworkClient and a NetworkServer.
 * This class acts as a wrapper around {@link io.netty.channel.socket.SocketChannel},
 * supplying various methods related to packet sending and managing.
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 24-1-2017
 */
public class PacketConnection {

	private long creation_time;
	private SocketChannel channel;
	private String name;
	private boolean authenticated;

	/**
	 * Create a new PacketConnection wrapper over the
	 * supplied SocketChannel
	 *
	 * @param channel SocketChannel
	 */
	public PacketConnection(SocketChannel channel) {
		this.channel = channel;
		this.name = "anonymous";
		this.creation_time = System.currentTimeMillis();
	}

	/**
	 * Create a new PacketConnection wrapper over the
	 * supplied SocketChannel
	 *
	 * @param channel SocketChannel
	 * @param name Connection name
	 */
	public PacketConnection(SocketChannel channel, String name) {
		this.channel = channel;
		this.name = name;
	}

	/**
	 * Returns the name of this connection
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the time at which this connection was established
	 *
	 * @return long
	 */
	public long getConnectionTime() {
		return creation_time;
	}

	/**
	 * Send a packet to this connection
	 *
	 * @param packet Packet
	 */
	public void sendPacket(Packet packet) {
		// Validate - Nullcheck
		NioValidate.isNull(packet, "Packet cannot be null");

		// Check if the socket channel is valid
		if(!channel.isActive() || !channel.isOpen()) {
			throw new NioNetworkException("[ERROR] Cannot send packet over closed channel! " +
				"Failed to send " + packet.getClass().getSimpleName() + " packet");
		}

		// Check if we can write
		if(!channel.isWritable()) {
			throw new NioNetworkException("Failed to write to channel");
		}

		// TODO: Packet send rule

		// Flush the packet down the pipeline
		channel.writeAndFlush(packet);
	}

	/**
	 * Disconnect the connection from the current NetworkInstance
	 */
	public void disconnect() {
		disconnect("Connection closed by remote");
	}

	/**
	 * Disconnect the connection from the current NetworkInstance
	 */
	public void disconnect(String msg) {
		channel.writeAndFlush(new PacketSystemDisconnect(msg))
			.addListener(ChannelFutureListener.CLOSE);

		NioUtil.nettyLog("Channel '" + name + "' disconnected: " + msg);
	}

	/**
	 * Returns true when this connection has succeeded authentication.
	 *
	 * @return boolean
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * Mark this connection as authenticated
	 *
	 * @param name connection name
	 */
	protected void setAuthenticated(String name) {
		this.authenticated = true;
		this.name = name;
	}

	/**
	 * Returns the channel inside this connection
	 *
	 * @return SocketChannel
	 */
	public SocketChannel channel() {
		return channel;
	}

}
