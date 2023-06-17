package org.seekers.grpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.grpc.net.Command;
import org.seekers.grpc.net.CommandRequest;
import org.seekers.grpc.net.CommandResponse;
import org.seekers.grpc.net.Empty;
import org.seekers.grpc.net.JoinRequest;
import org.seekers.grpc.net.JoinResponse;
import org.seekers.grpc.net.PropertiesResponse;
import org.seekers.grpc.net.SeekersGrpc;
import org.seekers.grpc.net.SeekersGrpc.SeekersBlockingStub;
import org.seekers.grpc.net.StatusResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.scene.paint.Color;

/**
 * The SeekersJavaClient class is a client for interacting with the Seekers game
 * server. It provides methods for joining the game, retrieving properties,
 * getting status updates, and setting commands for the game.
 *
 * @author karlz
 */
public class SeekersJavaClient {
	private static final Logger logger = Logger.getLogger(SeekersJavaClient.class.getName());

	private final ManagedChannel channel;
	private final SeekersBlockingStub seekersBlockingStub;

	private final SeekersStoreHelper helper = new SeekersStoreHelper();

	/**
	 * Initializes the SeekersJavaClient by creating a channel and stubs for
	 * communication with the server. It also starts the client by joining the game.
	 */
	public SeekersJavaClient() {
		channel = ManagedChannelBuilder.forAddress("localhost", 7777).usePlaintext().build();
		seekersBlockingStub = SeekersGrpc.newBlockingStub(channel);
		start();
	}

	/**
	 * Starts the client by joining the game with a specified name and color.
	 *
	 * @param name  The name of the player.
	 * @param color The color of the player.
	 */
	public void start() {
		join(toString(), Color.color(Math.random(), Math.random(), Math.random()).toString());
		logger.info("Client started");
	}

	/**
	 * Stops the client by shutting down the channel.
	 *
	 * @throws InterruptedException if the shutdown process is interrupted.
	 */
	public void stop() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		logger.info("Client shutdown");
	}

	private String token = "";
	private String playerId = "";

	/**
	 * Joins the Seekers game with the specified name and color.
	 *
	 * @param name  The name of the player.
	 * @param color The color of the player.
	 */
	public void join(String name, String color) {
		try {
			JoinResponse reply = seekersBlockingStub
					.join(JoinRequest.newBuilder().putAllDetails(Map.of("name", name, "color", color)).build());
			token = reply.getToken();
			playerId = reply.getPlayerId();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Retrieves the SeekersStoreHelper associated with the client.
	 *
	 * @return The SeekersStoreHelper.
	 */
	public SeekersStoreHelper getHelper() {
		return helper;
	}

	/**
	 * Retrieves the properties of the Seekers game from the server.
	 *
	 * @return The PropertiesResponse containing the game properties.
	 */
	public PropertiesResponse getProperties() {
		try {
			return seekersBlockingStub.properties(Empty.newBuilder().build());
		} catch (Exception ex) {
			return PropertiesResponse.newBuilder().build();
		}
	}

	/**
	 * Retrieves the status of the Seekers game from the server. Updates the
	 * SeekersStoreHelper with the received status response.
	 *
	 * @return The StatusResponse containing the game status.
	 */
	public StatusResponse getStatus() {
		try {
			StatusResponse response = seekersBlockingStub.status(Empty.newBuilder().build());
			helper.update(response);
			return response;
		} catch (Exception ex) {
			return StatusResponse.newBuilder().build();
		}
	}

	/**
	 * Sets a command for a specific seeker in the Seekers game.
	 *
	 * @param commands The commands with the ID, target vector and magnet value for
	 *                 the seeker.
	 * @return The CommandResponse indicating the success of the command.
	 */
	public CommandResponse setCommand(Iterable<? extends Command> commands) {
		try {
			return seekersBlockingStub
					.command(CommandRequest.newBuilder().setToken(token).addAllCommands(commands).build());
		} catch (Exception ex) {
			return CommandResponse.newBuilder().build();
		}
	}

	/**
	 * Retrieves the token assigned to the client.
	 *
	 * @return The token.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Retrieves the ID of the player associated with the client.
	 *
	 * @return The player ID.
	 */
	public String getPlayerId() {
		return playerId;
	}
}
