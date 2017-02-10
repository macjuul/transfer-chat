package net.exodiusmc.platformer.nio;

import io.netty.buffer.ByteBuf;
import net.exodiusmc.platformer.nio.exception.NioNetworkException;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static utility class containing various methods that should be used
 * when working with the network code.
 *
 * @author Macjuul
 * @version 1.1.0
 * @since 14-1-2017
 */
public class NioUtil {

	public static final Level NETTY = new LogLevel("Netty", Level.INFO.intValue());

	// Disabled constructor
	private NioUtil() {
	}

	/**
	 * Read a string from the ByteBuf
	 *
	 * @param buffer Buffer
	 * @return String
	 */
	public static String readString(ByteBuf buffer) {
		// Get the length of the next string
		short length = buffer.readShort();

		// Read da string
		return buffer.readBytes(length).toString(StandardCharsets.UTF_8);
	}

	/**
	 * Write the given string onto the ByteBuf
	 *
	 * @param buffer Buffer
	 * @param text String
	 */
	public static void writeString(ByteBuf buffer, String text) {
		writeVarBytes(buffer, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Writes a variable-lengthed byte array to the buffer. This
	 * method is different from {@link ByteBuf#writeBytes(byte[])}
	 * because the length of the byte array gets prepended before
	 * the actual array.
	 *
	 * @param buffer Buffer
	 * @param load Payload
	 */
	public static void writeVarBytes(ByteBuf buffer, byte[] load) {
		try {
			buffer.writeShort(load.length);
			buffer.writeBytes(load);
		} catch(Exception ex) {
			throw new NioNetworkException("Failed to write VarByte", ex);
		}

	}

	/**
	 * Read a variable-lengthed byte array from the buffer
	 *
	 * @param buffer Buffer
	 * @return Payload
	 */
	public static byte[] readVarBytes(ByteBuf buffer) {
		short len;

		try {
			len = buffer.readShort();
		} catch(Exception ex) {
			throw new NioNetworkException("Failed to read VarByte: Could not read length short", ex);
		}

		if(len < 0) {
			throw new NioNetworkException("Length Key was smaller than nothing!  Weird key!");
		} else {
			try {
				byte[] payload = new byte[len];

				buffer.readBytes(payload);

				return payload;
			} catch(Exception ex) {
				throw new NioNetworkException("Failed to read VarByte", ex);
			}
		}
	}

	/**
	 * Log a Netty-Level log to the stored network logger
	 *
	 * @param message Message
	 */
	public static void nettyLog(Logger logger, String message) {
		logger.log(Level.INFO, "[NET] " + message);
	}

	/**
	 * Log a Netty-Level log to the stored network logger
	 *
	 * @param message Message
	 * @param error Optional error
	 */
	public static void nettyLog(Logger logger, String message, Throwable error) {
		logger.log(Level.INFO, "[NET] " + message, error);
	}/**

	 /**
	 * Log a Netty-Level log to the stored network logger
	 *
	 * @param message Message
	 */
	public static void nettyLog(String message) {
		Logger.getAnonymousLogger().log(Level.INFO, "[NET] " + message);
	}

	/**
	 * Log a Netty-Level log to the stored network logger
	 *
	 * @param message Message
	 * @param error Optional error
	 */
	public static void nettyLog(String message, Throwable error) {
		Logger.getAnonymousLogger().log(NETTY, message, error);
	}

	/**
	 * Represents a log made regarding the network protocol
	 */
	public static class LogLevel extends Level {

		public LogLevel(String name, int value) {
			super(name, value);
		}
	}

}
