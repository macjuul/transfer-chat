package net.exodiusmc.platformer.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.exodiusmc.platformer.client.view.LoginView;
import net.exodiusmc.platformer.shared.nio.client.NetworkClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julian Mills
 * @version 1.0.0
 * @since 07/02/2017
 */
public class TransferClient extends Application {

    public static double HEIGHT = 800;
    public static double WIDTH = HEIGHT * 0.7984375;
    public static TransferClient instance;

    private Stage main_window;
    private List<Stage> windows;
    private String nickname;
    private Server remote;
    private NetworkClient client;

    @Override
    public void start(Stage window) throws Exception {
        TransferClient.instance = this;
        this.main_window = window;
        this.windows = new ArrayList<>();

        // Configure le main window
        window.setWidth(WIDTH);
        window.setHeight(HEIGHT);

        setWindowTitle(main_window, null);

        // Configure the window content
        new LoginView().apply(window);

        window.show();

        // Set the exit handler for the window.
        // Sometimes processes can keep the main window
        // open, causing the whole application to stay
        // active. To fix this, we'll exit the application
        // when the window closes.
        main_window.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Disconnect from the current server and
     * start up a new connection
     *
     * @param target Target server
     */
    public void connect(Server target) {
        // Disconnect
        if(client != null) {
            client.stop();
            client = null;
        }

        // Create the client
        client = NetworkClient.setup(target.getIp(), target.getPort(), nickname)

                // Register packets
                .packets(null)

                .buildAndStart();

        // Save the current server
        remote = target;
    }

    /**
     * Returns the nickname of the user
     *
     * @return String
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Set a new nickname for the user. This method will automatically
     * forward the nickname change to the server.
     *
     * @param nickname String
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Sets the title for the specified window
     *
     * @param window The window to apply the title to
     * @param title The title to apply
     * @return The given window
     */
    public static Stage setWindowTitle(Stage window, String title) {
        window.setTitle(title == null ? "Platformer" : "Platformer - " + title);

        return window;
    }

    /**
     * Returns the main application window
     *
     * @return Main window
     */
    public Stage getMainWindow() {
        return main_window;
    }

    /**
     * Returns a list of of windows
     *
     * @return a list of windows
     */
    public List<Stage> getWindows() {
        return windows;
    }

    /**
     * Returns the TransferClient
     *
     * @return Instance
     */
    public static TransferClient getClient() {
        return instance;
    }
}
