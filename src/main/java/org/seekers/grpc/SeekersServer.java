package org.seekers.grpc;

import static org.seekers.grpc.Corresponding.transform;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.game.Game;
import org.seekers.game.Player;
import org.seekers.game.Seeker;
import org.seekers.grpc.SeekersGrpc.SeekersImplBase;

import com.google.common.hash.Hashing;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import javafx.geometry.Point2D;

public class SeekersServer {
	private static final Logger logger = Logger.getLogger(SeekersServer.class.getName());

	private final Server server;

	private Game game = new Game(new File("server.properties"));

	public SeekersServer() {
		server = ServerBuilder.forPort(7777).addService(new SeekersService()).build();
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws Exception {
		server.start();
		game.getClock().start();
		logger.info("Server started, listening on " + server.getPort());
	}

	public void stop() throws Exception {
		if (server != null) {
			server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
			game.getClock().markAsDone();
			logger.info("Server shutdown");
		}
	}

	private class SeekersService extends SeekersImplBase {
		private final Map<String, Player> players = new HashMap<>();

		@Override
		public void join(JoinRequest request, StreamObserver<JoinReply> responseObserver) {
			if (game.hasOpenSlots()) {
				Player player = game.addPlayer();
				String token = Hashing.fingerprint2011().hashString("" + Math.random(), Charset.defaultCharset())
						.toString();
				players.put(token, player);
				responseObserver.onNext(JoinReply.newBuilder().setId(player.getId()).setToken(token).build());
				responseObserver.onCompleted();
			} else {
				responseObserver.onError(new StatusException(Status.RESOURCE_EXHAUSTED));
			}
		}

		@Override
		public void properties(PropertiesRequest request, StreamObserver<PropertiesReply> responseObserver) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			PropertiesReply reply = PropertiesReply.newBuilder().putAllEntries((Map) game.getProperties()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void status(StatusRequest request, StreamObserver<StatusReply> responseObserver) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			StatusReply reply = StatusReply.newBuilder().putAllPlayers(transform((Map) game.getPlayers()))
					.putAllCamps(transform((Map) game.getCamps())).putAllSeekers(transform((Map) game.getSeekers()))
					.putAllGoals(transform((Map) game.getGoals())).setPassedPlaytime(game.getPassedPlaytime()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void command(CommandRequest request, StreamObserver<CommandReply> responseObserver) {
			Player player = players.get(request.getToken());
			if (player != null) {
				Seeker seeker = player.getSeekers().get(request.getSeekerId());
				if (seeker != null) {
					seeker.setTarget(new Point2D(request.getTarget().getX(), request.getTarget().getY()));
					seeker.setMagnet(request.getMagnet());
					responseObserver.onNext(CommandReply.newBuilder().build());
					responseObserver.onCompleted();
				} else {
					responseObserver.onError(new StatusException(Status.NOT_FOUND));
				}
			} else {
				responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
			}
		}
	}
}
