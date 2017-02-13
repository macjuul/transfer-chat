package net.exodiusmc.platformer.client.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.exodiusmc.platformer.client.Server;
import net.exodiusmc.platformer.client.TransferClient;
import net.exodiusmc.platformer.shared.nio.HookType;
import net.exodiusmc.platformer.shared.nio.PacketConnection;
import net.exodiusmc.platformer.shared.nio.client.ClientChannelManager;
import net.exodiusmc.platformer.shared.nio.client.NetworkClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 08/02/2017
 */
public class LoginView implements View {

    @FXML
    private ChoiceBox<String> server_list;

    @FXML
    private Button connect;

    @FXML
    private TextField namefield;

    @FXML
    private Label error;

    @Override
    public void apply(Stage window) {
        window.setResizable(false);

        window.setScene(new Scene(View.templateFromFile("login", this)));

    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        System.out.println("LoginView - Loading content");

        // Set button activation handling
        namefield.textProperty().addListener((obs, old, value) -> connect.setDisable(value.isEmpty()));

        // Request server list
        requestServerList();

        // Add all servers to the dropdown
        int n = 1;
        for(Server server : Server.list()) {
            server_list.getItems().add("#" + n + " - " + server.getName());
            n++;
        }

        // Set the default selected item
        server_list.getSelectionModel().select(0);

        // Set the on connect handler
        connect.setOnMouseClicked(e -> {
        	// Disable button
	        connect.setDisable(true);
	        error.setText("");

            // Get the server
            int selected_server = server_list.getSelectionModel().getSelectedIndex();
            Server server = Server.list().get(selected_server);

            // Get the nickname
            String nickname = namefield.getText();

            TransferClient client = TransferClient.getClient();

            // Save the nickname
            client.setNickname(nickname);

            // Connect to le server
            client.connect(server);

            // Register hooks
	        NetworkClient net = client.getNetClient();
	        ClientChannelManager mngr = net.channelManager();

	        Consumer<PacketConnection> fail = mngr.registerHook(HookType.DISCONNECTED, conn -> {
		        Platform.runLater(() -> {
			        // Display an error
			        error.setText("Connection refused: Failed to connect");

			        // Shutdown the client
			        net.stop();

			        // Enable button
			        connect.setDisable(false);
		        });
	        });

	        mngr.registerHook(HookType.CONNECTED, packetConnection -> mngr.unregisterHook(HookType.DISCONNECTED, fail));
        });
    }

	/**
	 * Request the server list to be downloaded
	 */
	private void requestServerList() {
	    Pattern regex = Pattern.compile("(.+?)\\s+(.+?)\\s+(\\d+)");

	    try {
		    URL servers = new URL("https://raw.githubusercontent.com/macjuul/transfer-chat/master/servers");

		    BufferedReader in = new BufferedReader(new InputStreamReader(servers.openStream()));

		    String line;
		    while((line = in.readLine()) != null) {
			    Matcher m = regex.matcher(line);

			    // Find a new match
			    if(!m.find()) continue;

			    System.out.println("Discovered server '" + line + "' (Groups " + m.groupCount() + ")");

			    // Create a new server
			    Server.create(m.group(1), m.group(2), Short.valueOf(m.group(3)));
		    }

		    in.close();
	    } catch (Exception e) {
		    System.out.println("Failed to retrieve server list");
		    e.printStackTrace();
	    }
    }
}
