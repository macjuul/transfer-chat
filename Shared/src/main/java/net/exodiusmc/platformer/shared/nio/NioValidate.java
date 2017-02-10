package net.exodiusmc.platformer.shared.nio;

import net.exodiusmc.platformer.shared.nio.exception.NioValidationException;

/**
 * Misc validation methods and tools used by the network code
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 10/02/2017
 */
public class NioValidate {

    // Disabled constructor
    private NioValidate() {
    }

    /**
     * Returns true when the supplied object is null
     *
     * @param check Validation check
     * @return boolean
     */
    public static boolean isNull(Object check) {
        return check == null;
    }
    /**
     * Throws an exception with the specified message
     * when the object is null.
     *
     * @param check Validation check
     * @throws NioValidationException when check is null
     */
    public static void isNull(Object check, String error) {
        if(check == null) {
            throw new NioValidationException("Null check failed: " + error);
        }
    }

}
