package net.exodiusmc.platformer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.exodiusmc.platformer.view.LoginView;

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

    private Stage main_window;
    private List<Stage> windows;

    @Override
    public void start(Stage window) throws Exception {
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
}
