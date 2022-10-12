package org.seekers.grpc;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.grpc.RemoteControlGrpc.RemoteControlImplBase;
import org.seekers.world.Seeker;
import org.seekers.world.World;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import javafx.geometry.Point2D;

public class RemoteServer {
	private static final Logger logger = Logger.getLogger(RemoteServer.class.getName());

	private final Server server;

	private World world = new World(new File("server.properties"));

	public RemoteServer() {
		server = ServerBuilder.forPort(7777).addService(new RemoteService(world)).build();
		try {
			start();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		clock();
	}

	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

	public void clock() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (RemoteServer.this.isRunning()) {
					RemoteServer.this.world.getUpdater().tick();
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						logger.warning(e.getMessage());
					}
				}
				try {
					stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void start() throws Exception {
		server.start();
		logger.info("Server started, listening on " + server.getPort());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					RemoteServer.this.stop();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		});
	}

	public void stop() throws Exception {
		if (server != null) {
			server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
			executor.shutdown();
			System.err.println("Server shutdown");
		}
	}

	private boolean isRunning() {
		return world.getRemainingPlaytime() > 0;
	}

	private static class RemoteService extends RemoteControlImplBase {
		private World world;

		public RemoteService(World world) {
			this.world = world;
		}

		@Override
		public void sessionStatus(SessionRequest request, StreamObserver<SessionReply> responseObserver) {
			if (request.getToken().isBlank()) {
				// TODO register player
			}
			responseObserver.onNext(SessionReply.newBuilder().setPlaytime(world.getRemainingPlaytime()).build());
			responseObserver.onCompleted();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void entityStatus(EntityRequest request, StreamObserver<EntityReply> responseObserver) {
			responseObserver.onNext(EntityReply.newBuilder().putAllSeekers(Buildable.map((Map) world.getSeekers()))
					.putAllGoals(Buildable.map((Map) world.getGoals())).build());
			responseObserver.onCompleted();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void playerStatus(PlayerRequest request, StreamObserver<PlayerReply> responseObserver) {
			responseObserver
					.onNext(PlayerReply.newBuilder().putAllPlayers(Buildable.map((Map) world.getPlayers())).build());
			responseObserver.onCompleted();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void worldStatus(WorldRequest request, StreamObserver<WorldReply> responseObserver) {
			responseObserver.onNext(WorldReply.newBuilder().setWidth(world.getWidth()).setHeight(world.getHeight())
					.putAllCamps(Buildable.map((Map) world.getCamps())).build());
			responseObserver.onCompleted();
		}

		@Override
		public void commandUnit(CommandRequest request, StreamObserver<CommandReply> responseObserver) {
			Seeker seeker = world.getSeekers().get(request.getId());
			if (seeker == null) {
				responseObserver.onError(new StatusException(Status.NOT_FOUND));
			} else if (!request.getToken().contentEquals(seeker.getPlayer().getToken())) {
				responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
			} else {
				seeker.setTarget(new Point2D(request.getTarget().getX(), request.getTarget().getY()));
				seeker.getMagnet().setMode(request.getMagnet());

				responseObserver.onNext(CommandReply.newBuilder().setMessage(seeker.toString()).build());
			}
			responseObserver.onCompleted();
		}
	}
}
