package org.seekers.grpc;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.game.Game;
import org.seekers.game.Seeker;
import org.seekers.grpc.SeekersGrpc.SeekersImplBase;

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
		@Override
		public void joinSession(SessionRequest request, StreamObserver<SessionReply> responseObserver) {
			if (request.getToken().isBlank()) {
				responseObserver.onError(new StatusException(Status.UNAUTHENTICATED));
			} else if (game.hasOpenSlots()) {
				responseObserver.onNext(SessionReply.newBuilder().setId(game.addPlayer(request.getToken())).build());
			} else {
				responseObserver.onError(new StatusException(Status.RESOURCE_EXHAUSTED));
			}
			responseObserver.onCompleted();
		}

		@Override
		public void propertiesInfo(PropertiesRequest request, StreamObserver<PropertiesReply> responseObserver) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			PropertiesReply reply = PropertiesReply.newBuilder().putAllEntries((Map) game.getProperties()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void entityStatus(EntityRequest request, StreamObserver<EntityReply> responseObserver) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			EntityReply reply = EntityReply.newBuilder().putAllSeekers(Buildable.map((Map) game.getSeekers()))
					.putAllGoals(Buildable.map((Map) game.getGoals())).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void playerStatus(PlayerRequest request, StreamObserver<PlayerReply> responseObserver) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			PlayerReply reply = PlayerReply.newBuilder().putAllPlayers(Buildable.map((Map) game.getPlayers()))
					.putAllCamps(Buildable.map((Map) game.getCamps())).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void commandUnit(CommandRequest request, StreamObserver<CommandReply> responseObserver) {
			Seeker seeker = game.getSeekers().get(request.getId());
			if (seeker == null) {
				responseObserver.onError(new StatusException(Status.NOT_FOUND));
			} else if (!request.getToken().contentEquals(seeker.getPlayer().getToken())) {
				responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
			} else {
				seeker.setTarget(new Point2D(request.getTarget().getX(), request.getTarget().getY()));
				seeker.setMagnet(request.getMagnet());
			}
			responseObserver.onCompleted();
		}
	}
}
