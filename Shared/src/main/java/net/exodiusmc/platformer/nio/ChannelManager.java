package net.exodiusmc.platformer.nio;

import com.google.common.collect.BiMap;
import com.google.common.collect.ListMultimap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.exodiusmc.platformer.nio.pipeline.InboundPacketDecoder;
import net.exodiusmc.platformer.nio.pipeline.InboundTriggerHandler;
import net.exodiusmc.platformer.nio.pipeline.OutboundPacketEncoder;

import java.util.List;
import java.util.function.Consumer;

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
	private BiMap<Byte, Class<? extends Packet>> packets;
	private ListMultimap<HookType, Consumer<PacketConnection>> hooks;

	/**
	 * Create a new ChannelManager for the given NetworkInstance
	 *
	 * @param parent NetworkInstance
	 * @param hooks Hook Multimap
	 */
	public ChannelManager(NetworkInstance parent, ListMultimap<HookType, Consumer<PacketConnection>> hooks, BiMap<Byte, Class<? extends Packet>> packets) {
		this.parent = parent;
		this.hooks = hooks;
		this.packets = packets;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		// Log - setup
		NioUtil.nettyLog(parent.logger(), "Initializing new channel...");
		
		// Setup the codec
		setupPipeline(channel);
		
		// Set the disconnect handling
		channel.closeFuture().addListener((ChannelFuture future) -> {
			channelDisconnected((SocketChannel) future.channel());

			// log - destroy
			NioUtil.nettyLog(parent.logger(), "channel successfully disconnected");
		});

		// Call channel setup
		setupChannel(channel);

		// Log - complete
		NioUtil.nettyLog(parent.logger(), "channel successfully setup");
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
		pipe.addLast("InboundPacketDecoder", new InboundPacketDecoder(this));

		// Encoders
		pipe.addLast("LengthEncoder", new LengthFieldPrepender(4));
		pipe.addLast("OutboundPacketEncoder", new OutboundPacketEncoder(this));

		// The InboundTrigger channel handler will be called
		// with a finished Packet object. The channel will
		// tigger all subscribed listeners who are listening
		// for the packet.
		pipe.addLast("InboundTrigger", new InboundTriggerHandler(this));
	}

	/**
	 * Call all registered hooks of the specified HookType
	 *
	 * @param type HookType
	 * @param connection PacketConnection
	 * @return true when at least one hook was triggered
	 */
	public boolean callHook(HookType type, PacketConnection connection) {
		List<Consumer<PacketConnection>> triggers = hooks.get(type);

		// Return if none are hooked
		if(triggers == null || triggers.size() == 0) return false;

		for(Consumer<PacketConnection> conn : triggers) {
			conn.accept(connection);
		}

		return true;
	}

	/**
	 * Register a new hook
	 *
	 * @param type HookType
	 * @param handler handler
	 */
	public void registerHook(HookType type, Consumer<PacketConnection> handler) {
		this.hooks.put(type, handler);
	}

	/**
	 * Returns a BiMap of packets
	 *
	 * @return Packets
	 */
	public BiMap<Byte, Class<? extends Packet>> getPackets() {
		return packets;
	}

	/**
	 * Returns the parenting NetworkInstance
	 *
	 * @return NetworkInstance
	 */
	public NetworkInstance getParent() {
		return parent;
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
