package org.seekers.grpc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.game.Game;
import org.seekers.game.Player;
import org.seekers.game.Seeker;
import org.seekers.grpc.net.CommandRequest;
import org.seekers.grpc.net.CommandResponse;
import org.seekers.grpc.net.PropertiesRequest;
import org.seekers.grpc.net.PropertiesResponse;
import org.seekers.grpc.net.SeekersGrpc.SeekersImplBase;
import org.seekers.grpc.net.StatusRequest;
import org.seekers.grpc.net.StatusResponse;

import com.google.common.hash.Hashing;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import io.scvis.geometry.Vector2D;
import io.scvis.grpc.game.HostingGrpc.HostingImplBase;
import io.scvis.grpc.game.JoinRequest;
import io.scvis.grpc.game.JoinResponse;
import io.scvis.grpc.game.PingRequest;
import io.scvis.grpc.game.PingResponse;
import javafx.scene.paint.Color;

public class SeekersServer {
	private static final Logger logger = Logger.getLogger(SeekersServer.class.getName());

	private final Server server;

	public SeekersServer() {
		server = ServerBuilder.forPort(7777).addService(new HostingService()).addService(new SeekersService()).build();
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws IOException {
		server.start();
		logger.info("Server started");
	}

	public void stop() throws InterruptedException {
		server.shutdown().awaitTermination(5l, TimeUnit.SECONDS);
		logger.info("Server shutdown");
	}

	private Game game = new Game();

	private final Map<String, Player> players = new HashMap<>();

	protected class HostingService extends HostingImplBase {
		@Override
		public void join(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
			if (game.hasOpenSlots()) {
				Player player = game.addPlayer();
				player.setName(request.getDetailsMap().get("name"));
				player.setColor(Color.web(request.getDetailsMap().get("color")));
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
			PropertiesResponse reply = PropertiesResponse.newBuilder()
					.putAllEntries(SeekerProperties.getDefault().associated()).build();
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
					seeker.setTarget(new Vector2D(request.getTarget().getX(), request.getTarget().getY()));
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

	public Game getGame() {
		return game;
	}
}
