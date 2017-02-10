package net.exodiusmc.platformer.nio.exception;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 10/02/2017
 */
public class NioValidationException extends RuntimeException {

    public NioValidationException(String message) {
        super(message);
    }

    public NioValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
