package net.exodiusmc.platformer.shared.nio;

/**
 * Packet-specific information can be stored on PacketInfo objects.
 * These objects contain the packet id and packet class.
 *
 * @author Macjuul
 * @version 2.0.0
 * @since 26-1-2017
 */
public class PacketInfo {

	private byte id;
	private Class<? extends Packet> clazz;

	public PacketInfo(int id, Class<? extends Packet> clazz) {
		this.id = (byte) id;
		this.clazz = clazz;
	}

	/**
	 * Returns the packet id assigned to the packet
	 *
	 * @return byte id
	 */
	public byte getId() {
		return id;
	}

	/**
	 * Returns the packet class
	 *
	 * @return packet class
	 */
	public Class<? extends Packet> getPacketClass() {
		return clazz;
	}

}
