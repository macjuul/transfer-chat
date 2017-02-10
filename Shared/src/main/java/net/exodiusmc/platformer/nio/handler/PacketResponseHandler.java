package net.exodiusmc.platformer.nio.handler;

import net.exodiusmc.platformer.nio.PacketConnection;
import net.exodiusmc.platformer.nio.RespondablePacket;

/**
 * A listener interface used to execute a certain
 * piece of code when a specified packet gets called
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 12-1-2017
 */
public interface PacketResponseHandler extends PacketHandlerInterface {

	void receive(RespondablePacket packet, PacketConnection connection);

}
