package net.exodiusmc.platformer.shared.packets;

import io.netty.buffer.ByteBuf;
import net.exodiusmc.platformer.shared.nio.RespondablePacket;
import net.exodiusmc.platformer.shared.nio.RespondableTracker;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 15/02/2017
 */
public class RespondableTest extends RespondablePacket {

	@Override
	public RespondableTracker getRequest() {
		return new RespondableTracker() {

			private String a;

			{{{{{{{{{{{{{{{{{{{{{{{{
				String a = "A";
				String b = "B";

				this.a = a;
			}}}}}}}}}}}}}}}}}}}}}}}}

			@Override
			public void encodePayload(ByteBuf buffer) {
				System.out.println("A: " + a);
			}

			@Override
			public void decodePayload(ByteBuf buffer) {

			}

		};
	}

	@Override
	public RespondableTracker getResponse() {
		return new RespondableTracker() {

			@Override
			public void encodePayload(ByteBuf buffer) {

			}

			@Override
			public void decodePayload(ByteBuf buffer) {

			}

		};
	}
}
