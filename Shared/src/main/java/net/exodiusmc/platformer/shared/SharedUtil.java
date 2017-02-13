package net.exodiusmc.platformer.shared;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 13/02/2017
 */
public class SharedUtil {

    private static Logger logger;

    private SharedUtil() {
    }

    static {
        // Create logger
        Logger log = Logger.getLogger("Transfer");

        log.setUseParentHandlers(false);
        log.addHandler(new StdoutConsoleHandler());

        logger = log;
    }

    /**
     * Returns the logger used by the TransferClient and TransferServer
     *
     * @return Logger
     */
    public static Logger logger() {
        return logger;
    }

    public static class StdoutConsoleHandler extends ConsoleHandler {
        protected void setOutputStream(OutputStream out) throws SecurityException {
            super.setOutputStream(System.out); // kitten killed here :-(
        }
    }
}
