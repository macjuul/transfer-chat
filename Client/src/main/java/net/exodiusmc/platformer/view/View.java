package net.exodiusmc.platformer.view;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a view that can be applied to a Stage
 *
 * @author Macjuul
 * @version 1.0.0
 * @since 08/02/2017
 */
public interface View extends Initializable {

    void apply(Stage window);


    /**
     * Returns a URL for the specified FXML file
     *
     * @param name Template name
     * @return URL
     */
    static Parent templateFromFile(String name, Initializable controller) {
        try {
            URL url = getResource(name + ".fxml");

            if(url == null) throw new RuntimeException("URL could not be resolved");

            FXMLLoader loader = new FXMLLoader(url);
            loader.setController(controller);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not locate '" + name + "' fxml file");
        }
    }

    /**
     * Load a resource and receive it's URL
     *
     * @param name Path
     * @return URL
     */
    static URL getResource(String name) {
        return View.class.getClassLoader().getResource(name);
    }
}
