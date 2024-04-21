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
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.seekers.game.*;
import org.seekers.grpc.net.*;
import org.seekers.grpc.net.SeekersGrpc.SeekersImplBase;
import org.seekers.plugin.LanguageLoader;
import org.seekers.plugin.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The {@code SeekersServer} class represents the server-side implementation of the Seekers game. It provides the server
 * functionality for hosting the game, handling client requests, and managing game state. The server uses gRPC for
 * communication with clients.
 *
 * @author karlz
 * @author Supergecki
 * @see SeekersClient
 */
public class SeekersServer {
    private static final Logger logger = LoggerFactory.getLogger(SeekersServer.class);

    private final @Nonnull Server server; // gRPC server socket
    private final @Nonnull Stage stage; // Cache for close
    private final @Nonnull Game game; // Game
    private final @Nonnull Tournament tournament; // Tournament

    // Collections
    private final @Nonnull Map<String, Player> players = new HashMap<>();
    private final @Nonnull Set<SeekersClient> clients = new HashSet<>();
    private final @Nonnull Set<LanguageLoader> loaders = new HashSet<>();
    private final @Nonnull Map<String, String> properties = new HashMap<>();

    /**
     * Constructs a new {@code SeekersServer} instance for the port 7777.
     *
     * @param stage   the javafx stage to show the match
     * @param loaders the list of language loaders for loading the clients
     * @throws IOException if unable to bind
     */
    public SeekersServer(@Nonnull Stage stage, Collection<LanguageLoader> loaders) throws IOException {
        this.server = ServerBuilder.forPort(7777).addService(new SeekersService()).build();
        this.stage = stage;
        this.loaders.addAll(loaders);

        Ini ini = new Ini(new File("config.ini"));
        for (Map.Entry<String, Profile.Section> section : ini.entrySet()) {
            for (Map.Entry<String, String> entry : section.getValue().entrySet()) {
                properties.put(section.getKey() + '.' + entry.getKey(), entry.getValue());
            }
        }

        this.game = new Game(new BorderPane(), new Game.Properties(ini), new Camp.Properties(ini),
                new Seeker.Properties(ini), new Goal.Properties(ini));
        this.tournament = new Tournament("players");
        start();
    }

    /**
     * Starts the server and rotates the matching schedule of the tournament. This will start the game matches.
     *
     * @throws IOException if unable to bind
     */
    public void start() throws IOException {
        server.start();
        game.finishedProperty().addListener(c -> {
            if (game.finishedProperty().get()) {
                game.addToTournament(tournament);
                try {
                    tournament.save();
                    rotate();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        rotate();
        logger.info("Server started");
    }

    /**
     * Stops all old clients, logs the match results and closes the server.
     *
     * @throws InterruptedException if the shutdown is interrupted.
     * @throws IOException          if it could not close the clients
     */
    public void stop() throws InterruptedException, IOException {
        stopOldClients();
        logger.info("Match results: {}", tournament.getResults());
        server.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
        logger.info("Server shutdown");
    }

    /**
     * Stops all old clients. This will clear the list of connected clients.
     *
     * @throws IOException if it could not close the client
     */
    private void stopOldClients() throws IOException {
        logger.info("Stop old clients");
        for (SeekersClient client : clients) client.close();
        clients.clear();
    }

    /**
     * Tries to host a single file over a language loader. If no language loader was found that can host the specified
     * file, it must be hosted manually.
     *
     * @param file the name of the file
     */
    private void hostFile(String file) {
        for (LanguageLoader loader : loaders) {
            if (loader.canHost(file)) {
                SeekersClient client = loader.create();
                client.host(new File(file));
                clients.add(client);
                return;
            }
        }
        logger.error("Could not find loader for file {}", file);
    }

    /**
     * Hosts new clients for the next match.
     */
    private void hostNewClients() {
        logger.info("Host new clients");
        List<String> match = tournament.getMatches().remove(0);
        for (String file : match) {
            hostFile(file);
        }
    }

    /**
     * Resets the game and clears all connection data from the current players.
     */
    private void rebaseCached() {
        logger.info("Reset game and clear players");
        game.reset();
        players.clear();
    }

    /**
     * Rotates the matches. This will stop the current match. First stops all old clients, then restarts the game.
     * Finally, it will host new clients for the new match. If there are no matches left, it will close the server
     * instead.
     *
     * @throws IOException if it could not close the clients
     */
    public synchronized void rotate() throws IOException {
        logger.info("Rebase server");
        stopOldClients();
        if (!tournament.getMatches().isEmpty()) {
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
     * The {@code SeekersService} class handles the game-related gRPC service requests.
     */
    protected class SeekersService extends SeekersImplBase {

        /**
         * Handles the "properties" request from a client. Responds with a PropertiesResponse containing the default
         * seeker properties.
         *
         * @param request          The properties request.
         * @param responseObserver The response observer.
         */
        @Override
        public void properties(Empty request, StreamObserver<PropertiesResponse> responseObserver) {
            responseObserver.onNext(PropertiesResponse.newBuilder().putAllEntries(properties).build());
            responseObserver.onCompleted();
        }

        /**
         * Handles the "status" request from a client. Responds with a StatusResponse containing the associated game
         * data for the given token.
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
         * Handles the "command" request from a client. Updates the target and magnet properties of the specified
         * seeker.
         *
         * @param request          The command request.
         * @param responseObserver The response observer.
         * @apiNote Will throw {@code PERMISSION_DENIED} if the token is not valid. Commands that target seekers the
         * player does not control will be ignored. The seekers changed number only counts the number of seekers that
         * were successfully altered by the request.
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
         * Handles the "join" request from a client. If there are open slots in the game, a new player is added and
         * assigned a token. The player details are stored in the players map along with the associated token and
         * dispatch helper.
         *
         * @param request          The join request.
         * @param responseObserver The response observer.
         * @apiNote Will throw {@code RESOURCE_EXHAUSTED} if there are no player slots available.
         */
        @Override
        public synchronized void join(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
            if (game.hasOpenSlots()) {
                try {
                    Platform.runLater(() -> {
                        Player player = game.addPlayer();
                        player.setName(request.getDetailsMap().getOrDefault("name", "Unnamed Player"));
                        player.setColor(
                                Color.web(request.getDetailsMap().getOrDefault("color", player.getColor().toString())));
                        String token =
                                Hashing.fingerprint2011().hashString("" + Math.random(), Charset.defaultCharset())
                                        .toString();
                        players.put(token, player);

                        responseObserver.onNext(
                                JoinResponse.newBuilder().setPlayerId(player.getIdentifier()).setToken(token).build());
                        responseObserver.onCompleted();
                    });
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            } else {
                responseObserver.onError(new StatusException(Status.RESOURCE_EXHAUSTED));
            }
        }

        /**
         * Handles the "ping" request from a client. Responds with a PingResponse containing the current server
         * timestamp.
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
