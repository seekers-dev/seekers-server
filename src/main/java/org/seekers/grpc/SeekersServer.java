package org.seekers.grpc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.seekers.game.Game;
import org.seekers.game.Player;
import org.seekers.game.Seeker;
import org.seekers.grpc.net.Command;
import org.seekers.grpc.net.CommandRequest;
import org.seekers.grpc.net.CommandResponse;
import org.seekers.grpc.net.Empty;
import org.seekers.grpc.net.JoinRequest;
import org.seekers.grpc.net.JoinResponse;
import org.seekers.grpc.net.PingResponse;
import org.seekers.grpc.net.PropertiesResponse;
import org.seekers.grpc.net.SeekersGrpc.SeekersImplBase;
import org.seekers.grpc.net.StatusResponse;

import com.google.common.hash.Hashing;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import io.scvis.geometry.Vector2D;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * The SeekersServer class represents the server-side implementation of the
 * Seekers game. It provides the server functionality for hosting the game,
 * handling client requests, and managing game state. The server uses gRPC for
 * communication with clients.
 * 
 * @author karlz
 */
public class SeekersServer {
	private static final Logger logger = Logger.getLogger(SeekersServer.class.getName());

	private final Server server;

	private final Stage stage;

	/**
	 * Constructs a new SeekersServer instance.
	 */
	public SeekersServer(Stage stage) {
		this.server = ServerBuilder.forPort(7777).addService(new SeekersService()).build();
		this.stage = stage;
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the server.
	 *
	 * @throws IOException if unable to bind
	 */
	public void start() throws IOException {
		server.start();
		rotate();
		logger.info("Server started");
	}

	/**
	 * Stops the server.
	 *
	 * @throws InterruptedException if the shutdown is interrupted.
	 */
	public void stop() throws InterruptedException {
		stopOldClients();
		server.shutdown().awaitTermination(5l, TimeUnit.SECONDS);
		logger.info("Server shutdown");
	}

	private final SeekerTournament tournament = new SeekerTournament();

	private SeekersClient client0;
	private SeekersClient client1;

	private void stopOldClients() {
		logger.info("Stop old clients");
		if (client0 != null)
			client0.stop();
		if (client1 != null)
			client1.stop();
	}

	private void hostNewClients() {
		logger.info("Host new clients");
		final Pair<String, String> match = tournament.next();
		client0 = new SeekersClient(match.getKey());
		client1 = new SeekersClient(match.getValue());
	}

	private void rebaseCached() {
		logger.info("Reset game and clear players");
		Platform.runLater(() -> {
			game = new Game(new BorderPane(), SeekerProperties.getDefault().getMapWidth(),
					SeekerProperties.getDefault().getMapHeight());
			game.finishedProperty().addListener(c -> {
				game.addToTournament(tournament);
				logger.info("Current top list: " + tournament.getTopPlayers().toString());
				rotate();
			});
			stage.setScene(game);
		});
		players.clear();
	}

	public synchronized void rotate() {
		logger.info("Rebase server");
		stopOldClients();
		if (tournament.hasNext()) {
			rebaseCached();
			hostNewClients();
		} else {
			logger.info("No matchs left, closing server");
			try {
				stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
			Platform.runLater(stage::close);
			logger.info("Final results: " + tournament.getTopPlayers().toString());
		}
	}

	@Nonnull
	private Game game = new Game(new BorderPane(), SeekerProperties.getDefault().getMapWidth(),
			SeekerProperties.getDefault().getMapHeight());
	@Nonnull
	private final Map<String, Player> players = new HashMap<>();

	/**
	 * The SeekersService class handles the game-related gRPC service requests.
	 */
	protected class SeekersService extends SeekersImplBase {

		/**
		 * Handles the "properties" request from a client. Responds with a
		 * PropertiesResponse containing the default seeker properties.
		 *
		 * @param request          The properties request.
		 * @param responseObserver The response observer.
		 */
		@Override
		public void properties(Empty request, StreamObserver<PropertiesResponse> responseObserver) {
			PropertiesResponse reply = PropertiesResponse.newBuilder()
					.putAllEntries(SeekerProperties.getDefault().associated()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		/**
		 * Handles the "status" request from a client. Responds with a StatusResponse
		 * containing the associated game data for the given token.
		 *
		 * @param request          The status request.
		 * @param responseObserver The response observer.
		 */
		@Override
		public void status(Empty request, StreamObserver<StatusResponse> responseObserver) {
			responseObserver.onNext(game.getStatusResponse());
			responseObserver.onCompleted();
		}

		/**
		 * Handles the "command" request from a client. Updates the target and magnet
		 * properties of the specified seeker.
		 *
		 * @param request          The command request.
		 * @param responseObserver The response observer.
		 */
		@Override
		public void command(CommandRequest request, StreamObserver<CommandResponse> responseObserver) {
			Player player = players.get(request.getToken());
			if (player != null) {
				int changed = 0;
				for (Command command : request.getCommandsList()) {
					Seeker seeker = player.getSeekers().get(command.getSeekerId());
					if (seeker != null) {
						Vector2D target = new Vector2D(command.getTarget().getX(), command.getTarget().getY());
						if (seeker.getMagnet() != command.getMagnet() || !seeker.getTarget().equals(target)) {
							Platform.runLater(() -> {
								seeker.setTarget(target);
								seeker.setMagnet(command.getMagnet());
							});
							changed++;
						}
					}
				}
				responseObserver.onNext(CommandResponse.newBuilder().setStatus(game.getStatusResponse())
						.setSeekersChanged(changed).build());
				responseObserver.onCompleted();
			} else {
				responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
			}
		}

		/**
		 * Handles the "join" request from a client. If there are open slots in the
		 * game, a new player is added and assigned a token. The player details are
		 * stored in the players map along with the associated token and dispatch
		 * helper.
		 *
		 * @param request          The join request.
		 * @param responseObserver The response observer.
		 */
		@SuppressWarnings("null")
		@Override
		public synchronized void join(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
			if (game.hasOpenSlots()) {
				Player player = game.addPlayer();
				try {
					player.setName(request.getDetailsMap().getOrDefault("name", "Unnamed Player"));
					player.setColor(
							Color.web(request.getDetailsMap().getOrDefault("color", player.getColor().toString())));
				} catch (Exception e) {
					logger.warning("The details map has an issue: " + e.getMessage());
				}
				String token = Hashing.fingerprint2011().hashString("" + Math.random(), Charset.defaultCharset())
						.toString();
				players.put(token, player);

				responseObserver.onNext(JoinResponse.newBuilder().setPlayerId(player.getId()).setToken(token).build());
				responseObserver.onCompleted();
			} else {
				responseObserver.onError(new StatusException(Status.RESOURCE_EXHAUSTED));
			}
		}

		/**
		 * Handles the "ping" request from a client. Responds with a PingResponse
		 * containing the current server timestamp.
		 *
		 * @param request          The ping request.
		 * @param responseObserver The response observer.
		 */
		@Override
		public void ping(Empty request, StreamObserver<PingResponse> responseObserver) {
			responseObserver.onNext(PingResponse.newBuilder().setTimestamp(System.nanoTime()).build());
			responseObserver.onCompleted();
		}
	}

	/**
	 * Retrieves the game instance.
	 *
	 * @return The game.
	 */
	@Nonnull
	public Game getGame() {
		return game;
	}
}
