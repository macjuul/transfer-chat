package net.exodiusmc.platformer.shared;

import java.util.Random;

/**
 * Represents a generic chat message
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 10/02/2017
 */
public abstract class ChatMessage {

    private int id;

    /**
     * Create a new ChatMessage
     *
     * @param id Unique identifier
     */
    public ChatMessage(int id) {
        this.id = id;
    }

    /**
     * Returns the ChatMessage id for this message
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Create a new ChatMessage with a unique random identifier
     */
    public ChatMessage() {
        this.id = new Random().nextInt();
    }
}
