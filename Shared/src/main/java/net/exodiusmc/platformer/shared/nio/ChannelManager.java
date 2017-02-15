package net.exodiusmc.platformer.shared.nio;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.exodiusmc.platformer.shared.nio.pipeline.InboundPacketDecoder;
import net.exodiusmc.platformer.shared.nio.pipeline.InboundTriggerHandler;
import net.exodiusmc.platformer.shared.nio.pipeline.OutboundPacketEncoder;

/**
 * The Channel Manager takes care of all connected server channel connections. This class
 * is used for managing the different client-channels.
 *
 * @author Macjuul
 * @version 2.0.0
 * @since 02-10-2016
 */
public abstract class ChannelManager extends ChannelInitializer<SocketChannel> {

	private NetworkInstance parent;

	/**
	 * Create a new ChannelManager for the given NetworkInstance
	 *
	 * @param parent Parent NetworkInstance
	 */
	public ChannelManager(NetworkInstance parent) {
		this.parent = parent;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		// Setup the pipeline
		setupPipeline(channel);
		
		// Set the disconnect handling
		channel.closeFuture().addListener((ChannelFuture future) -> {
			channelDisconnected((SocketChannel) future.channel());

			// log - destroy
			NioUtil.nettyLog(parent.logger(), "channel successfully disconnected");
		});

		// Log - complete
		NioUtil.nettyLog(parent.logger(), "Setting up new connection");

		// Call channel setup
		setupChannel(channel);
	}

	/**
	 * Setup the codec for an initializing channel
	 *
	 * @param channel SocketChannel
	 */
	private void setupPipeline(SocketChannel channel) {
		ChannelPipeline pipe = channel.pipeline();

		// Decoders
		pipe.addLast("LengthDecoder", new LengthFieldBasedFrameDecoder(10000, 0, 4, 0, 4));
		pipe.addLast("InboundPacketDecoder", new InboundPacketDecoder(parent));

		// Encoders
		pipe.addLast("LengthEncoder", new LengthFieldPrepender(4));
		pipe.addLast("OutboundPacketEncoder", new OutboundPacketEncoder(parent));

		// The InboundTrigger channel handler will be called
		// with a finished Packet object. The channel will
		// trigger all subscribed listeners who are listening
		// for the packet.
		pipe.addLast("InboundTrigger", new InboundTriggerHandler(parent));
	}

	/**
	 * Lifecycle method: called when a new channel is setup
	 *
	 * @param channel the new SocketChannel
	 */
	public abstract void setupChannel(SocketChannel channel);

	/**
	 * Lifecycle method: called when an existing channel disconnects
	 *
	 * @param channel the channel to disconnect
	 */
	public abstract void channelDisconnected(SocketChannel channel);

	/**
	 * Used to mark a connection as 'authenticated'
	 */
	public static void identifyConnection(PacketConnection conn, String name) {
		conn.setAuthenticated(name);
	}

}
