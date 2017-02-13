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

	/**
	 * Returns true when the supplied number is less than min, or more than max.
	 * The number boundaries are exclusive.
	 *
	 * @param min Min
	 * @param max Max
	 * @param check Number to check
	 */
	public static boolean between(double min, double max, double check) {
		return min >= check || check >= max;
	}
	/**
	 * Throws an exception when the supplied number is less than min, or more than max.
	 * The number boundaries are exclusive.
	 *
	 * @param min Min
	 * @param max Max
	 * @param check Number to check
	 * @param error Error message to throw
	 */
	public static void between(double min, double max, double check, String error) {
		if(min >= check || check >= max) {
			throw new NioValidationException("Number range check failed: " + error);
		}
	}
	/**
	 * Returns true when the supplied number is less than min, or more than max.
	 * The number boundaries are inclusive.
	 *
	 * @param min Min
	 * @param max Max
	 * @param check Number to check
	 */
	public static boolean betweenInc(double min, double max, double check) {
		return min > check || check > max;
	}
	/**
	 * Throws an exception when the supplied number is less than min, or more than max.
	 * The number boundaries are inclusive.
	 *
	 * @param min Min
	 * @param max Max
	 * @param check Number to check
	 * @param error Error message to throw
	 */
	public static void betweenInc(double min, double max, double check, String error) {
		if(min > check || check > max) {
			throw new NioValidationException("Number range check failed: " + error);
		}
	}

}
