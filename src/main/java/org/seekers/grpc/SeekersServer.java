package org.seekers.grpc;

import com.google.common.hash.Hashing;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.seekers.game.Game;
import org.seekers.game.Player;
import org.seekers.game.Seeker;
import org.seekers.grpc.net.*;
import org.seekers.grpc.net.SeekersGrpc.SeekersImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The SeekersServer class represents the server-side implementation of the
 * Seekers game. It provides the server functionality for hosting the game,
 * handling client requests, and managing game state. The server uses gRPC for
 * communication with clients.
 *
 * @author karlz
 */
public class SeekersServer {
    private static final Logger logger = LoggerFactory.getLogger(SeekersServer.class);

    private final @Nonnull Server server;
    private final @Nonnull Stage stage;
    private final @Nonnull SeekersTournament tournament = new SeekersTournament();
    private final @Nonnull Map<String, Player> players = new HashMap<>();

	private @Nonnull Game game = new Game(new BorderPane(), SeekersConfig.getConfig().getMapWidth(),
			SeekersConfig.getConfig().getMapHeight());
	private @Nullable SeekersClient client0;
    private @Nullable SeekersClient client1;

    /**
     * Constructs a new SeekersServer instance.
     */
    public SeekersServer(@Nonnull Stage stage) throws IOException {
        this.server = ServerBuilder.forPort(7777).addService(new SeekersService()).build();
        this.stage = stage;
        start();
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
        server.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
        logger.info("Server shutdown");
    }

    private void stopOldClients() {
        logger.info("Stop old clients");
        if (client0 != null)
            client0.stop();
        if (client1 != null)
            client1.stop();
    }

    private void hostNewClients() throws IOException {
        logger.info("Host new clients");
        SeekersTournament.Match match = tournament.next();
        Iterator<String> iterator = match.getMembers().keySet().iterator();
        client0 = new SeekersClient(iterator.next());
        client1 = new SeekersClient(iterator.next());
    }

    private void rebaseCached() {
        logger.info("Reset game and clear players");
        Platform.runLater(() -> {
            game = new Game(new BorderPane(), SeekersConfig.getConfig().getMapWidth(),
                    SeekersConfig.getConfig().getMapHeight());
            game.finishedProperty().addListener(c -> {
                game.addToTournament(tournament);
                logger.info("Current top list: {}", tournament.getTopPlayers());
                try {
                    tournament.save();
                    rotate();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            });
            stage.setScene(game);
        });
        players.clear();
    }

    public synchronized void rotate() throws IOException {
        logger.info("Rebase server");
        stopOldClients();
        if (tournament.hasNext()) {
            rebaseCached();
            hostNewClients();
        } else {
            logger.info("No matches left, closing server");
            try {
                stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(stage::close);
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
                    .putAllEntries(SeekersConfig.getConfig().associated()).build();
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
                        Point2D target = new Point2D(command.getTarget().getX(), command.getTarget().getY());
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
        @Override
        public synchronized void join(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
            if (game.hasOpenSlots()) {
                Player player = game.addPlayer();
                try {
                    Platform.runLater(() -> {
                        player.setName(request.getDetailsMap().getOrDefault("name", "Unnamed Player"));
                        player.setColor(
                                Color.web(request.getDetailsMap().getOrDefault("color", player.getColor().toString())));
                    });
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
                String token = Hashing.fingerprint2011().hashString("" + Math.random(), Charset.defaultCharset())
                        .toString();
                players.put(token, player);

                responseObserver.onNext(JoinResponse.newBuilder().setPlayerId(player.getIdentifier()).setToken(token).build());
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
}
