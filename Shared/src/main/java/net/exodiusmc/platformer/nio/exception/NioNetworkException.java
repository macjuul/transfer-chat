package net.exodiusmc.platformer.nio.exception;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 10/02/2017
 */
public class NioNetworkException extends RuntimeException {

    public NioNetworkException(String message) {
        super(message);
    }

    public NioNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
