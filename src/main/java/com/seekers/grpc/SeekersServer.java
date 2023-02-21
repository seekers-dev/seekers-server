package com.seekers.grpc;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.hash.Hashing;
import com.karlz.bounds.Vector;
import com.karlz.grpc.exchange.HostingGrpc.HostingImplBase;
import com.karlz.grpc.exchange.JoinRequest;
import com.karlz.grpc.exchange.JoinResponse;
import com.karlz.grpc.exchange.PingRequest;
import com.karlz.grpc.exchange.PingResponse;
import com.seekers.game.Game;
import com.seekers.game.Player;
import com.seekers.game.Seeker;
import com.seekers.grpc.game.CommandRequest;
import com.seekers.grpc.game.CommandResponse;
import com.seekers.grpc.game.PropertiesRequest;
import com.seekers.grpc.game.PropertiesResponse;
import com.seekers.grpc.game.SeekersGrpc.SeekersImplBase;
import com.seekers.grpc.game.StatusRequest;
import com.seekers.grpc.game.StatusResponse;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

public class SeekersServer {
	private static final Logger logger = Logger.getLogger(SeekersServer.class.getName());

	private final Server server;

	private Game game = new Game(new File("server.properties"));

	public SeekersServer() {
		server = ServerBuilder.forPort(7777).addService(new HostingService()).addService(new SeekersService()).build();
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
		server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		game.getClock().markAsDone();
		logger.info("Server shutdown");
	}

	private final Map<String, Player> players = new HashMap<>();

	class HostingService extends HostingImplBase {
		@Override
		public void join(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
			String name = request.getDetailsMap().get("name");
			String color = request.getDetailsMap().get("color");
			if (name.isEmpty() && color.isEmpty()) {
				String token = Hashing.fingerprint2011().hashString("" + Math.random(), Charset.defaultCharset())
						.toString();
				game.getHelpers().put(token, new SeekersDispatchHelper(game));
				responseObserver.onNext(JoinResponse.newBuilder().setToken(token).build());
				responseObserver.onCompleted();
			} else if (game.hasOpenSlots()) {
				Player player = game.addPlayer();
				String token = Hashing.fingerprint2011().hashString("" + Math.random(), Charset.defaultCharset())
						.toString();
				game.getHelpers().put(token, new SeekersDispatchHelper(game));
				players.put(token, player);
				responseObserver.onNext(JoinResponse.newBuilder().setPlayerId(player.getId()).setToken(token).build());
				responseObserver.onCompleted();
			} else {
				responseObserver.onError(new StatusException(Status.RESOURCE_EXHAUSTED));
			}
		}

		@Override
		public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
			responseObserver.onNext(PingResponse.newBuilder().setPing(System.currentTimeMillis()).build());
			responseObserver.onCompleted();
		}
	}

	protected class SeekersService extends SeekersImplBase {
		@Override
		public void properties(PropertiesRequest request, StreamObserver<PropertiesResponse> responseObserver) {
			@SuppressWarnings("unchecked")
			PropertiesResponse reply = PropertiesResponse.newBuilder()
					.putAllEntries((Map<String, String>) (Map<?, ?>) game.getProperties()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void status(StatusRequest request, StreamObserver<StatusResponse> responseObserver) {
			SeekersDispatchHelper helper = game.getHelpers().get(request.getToken());
			if (helper != null) {
				responseObserver.onNext(helper.associated());
				responseObserver.onCompleted();
			} else {
				responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
			}
		}

		@Override
		public void command(CommandRequest request, StreamObserver<CommandResponse> responseObserver) {
			Player player = players.get(request.getToken());
			if (player != null) {
				Seeker seeker = player.getSeekers().get(request.getSeekerId());
				if (seeker != null) {
					seeker.setTarget(new Vector(request.getTarget().getX(), request.getTarget().getY()));
					seeker.setMagnet(request.getMagnet());
					responseObserver.onNext(CommandResponse.newBuilder().build());
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
