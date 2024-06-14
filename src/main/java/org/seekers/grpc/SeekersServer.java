/*
 * Copyright (C) 2022  Seekers Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.seekers.grpc;

import com.google.common.hash.Hashing;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.seekers.game.*;
import org.seekers.grpc.service.*;
import org.seekers.plugin.GameMode;
import org.seekers.plugin.ClientLoader;
import org.seekers.game.Tournament;
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
    private final @Nonnull Ini config;

    // Collections
    private final @Nonnull Map<String, Player> players = new HashMap<>();
    private final @Nonnull Set<SeekersClient> clients = new HashSet<>();
    private final @Nonnull Set<ClientLoader> loaders = new HashSet<>();
    private final @Nonnull List<Section> sections = new ArrayList<>();

    private GameMode mode;
    private Game game; // Game
    private Tournament tournament; // Tournament

    /**
     * Constructs a new {@code SeekersServer} instance for the port 7777.
     *
     * @param stage   the javafx stage to show the match
     * @param config  the config
     */
    public SeekersServer(@Nonnull Stage stage, @Nonnull Ini config) {
        this.server = ServerBuilder.forPort(7777).addService(new SeekersService()).build();
        this.stage = stage;
        this.config = config;

        for (Map.Entry<String, Profile.Section> section : config.entrySet()) {
            sections.add(Section.newBuilder().setName(section.getKey()).putAllEntries(section.getValue()).build());
        }
    }

    /**
     * Starts the server and rotates the matching schedule of the tournament. This will start the game matches.
     *
     * @throws IOException if unable to bind
     */
    public void start() throws IOException {
        Objects.requireNonNull(mode);
        Objects.requireNonNull(tournament);

        server.start();
        game = mode.createGame(new Game.Properties(config), new Camp.Properties(config), new Seeker.Properties(config),
                new Goal.Properties(config));
        stage.setScene(game);
        game.setOnGameStarted(g -> {
            logger.info("Game started with players {}", g.getPlayers());
            game.getTimeline().playFromStart();
        });
        game.setOnGameFinished(g -> {
            g.addToTournament(tournament);
            try {
                tournament.save();
                rotate();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
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
        for (ClientLoader loader : loaders) {
            if (loader.canHost(file)) {
                SeekersClient client = loader.create();
                client.host(new File(file));
                clients.add(client);
                return;
            }
        }
        logger.warn("Could not find loader for file {}", file);
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

    public SeekersServer setGameMode(@Nonnull GameMode mode) {
        this.mode = mode;
        return this;
    }

    public SeekersServer setTournament(@Nonnull Tournament tournament) {
        this.tournament = tournament;
        return this;
    }

    public SeekersServer addClientLoaders(@Nonnull Collection<ClientLoader> loaders) {
        this.loaders.addAll(loaders);
        return this;
    }

    /**
     * The {@code SeekersService} class handles the game-related gRPC service requests.
     */
    protected class SeekersService extends SeekersGrpc.SeekersImplBase {

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
                for (Command command : request.getCommandsList()) {
                    Seeker seeker = player.getSeekers().get(command.getSeekerId());
                    if (seeker != null) {
                        Point2D target = new Point2D(command.getTarget().getX(), command.getTarget().getY());
                        if (seeker.getMagnet() != command.getMagnet() || !seeker.getTarget().equals(target)) {
                            Platform.runLater(() -> {
                                seeker.setTarget(target);
                                seeker.setMagnet(command.getMagnet());
                            });
                        }
                    }
                }
                player.setObserver(responseObserver);
            } else {
                logger.error("Player {} is not part of the game", request.getToken());
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
                Platform.runLater(() -> {
                    try {
                        Player player = mode.createPlayer(game);
                        if (request.hasName() && !request.getName().isBlank()) {
                            logger.info("INFO: Used name {}", request.getName());
                            player.setName(request.getName());
                        }
                        if (request.hasColor() && !request.getColor().isBlank()) {
                            logger.info("INFO: Used color {}", request.getColor());
                            player.setColor(Color.web(request.getColor()));
                        }
                        String token = Hashing.fingerprint2011().hashString("" + Math.random(),
                                Charset.defaultCharset()).toString();
                        players.put(token, player);

                        responseObserver.onNext(JoinResponse.newBuilder().setPlayerId(player.getIdentifier())
                                .setToken(token).addAllSections(sections).build());
                        responseObserver.onCompleted();
                    } catch (Exception e) {
                        responseObserver.onError(e);
                        logger.warn(e.getMessage(), e);
                    } finally {
                        if (!game.hasOpenSlots()) {
                            game.setGameState(GameState.RUNNING);
                        }
                    }
                });
            } else {
                logger.error("Player {} tried to join game, but the game is already full", request.getName());
                responseObserver.onError(new StatusException(Status.RESOURCE_EXHAUSTED));
            }
        }

    }
}
