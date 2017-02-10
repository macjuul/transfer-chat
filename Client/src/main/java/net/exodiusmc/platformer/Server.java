package net.exodiusmc.platformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 10/02/2017
 */
public class Server {

    private static final List<Server> server_list = new ArrayList<>();

    private String name;
    private String ip;
    private short port;

    /**
     * Create a new Server
     *
     * @param name Server name
     * @param ip Target ip
     * @param port Port
     */
    private Server(String name, String ip, short port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Returns the name of this server
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the target ip of this server
     *
     * @return String
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns the port of this server, mostly 19284
     *
     * @return Port
     */
    public short getPort() {
        return port;
    }

    /**
     * Create a new Server
     *
     * @param name Server name
     * @param ip Target ip
     * @param port Port
     */
    public static void create(String name, String ip, short port) {
        server_list.add(new Server(name, ip, port));
    }

    /**
     * Returns a list of servers
     *
     * @return Unmodifiable Server list
     */
    public static List<Server> list() {
        return Collections.unmodifiableList(server_list);
    }
}
