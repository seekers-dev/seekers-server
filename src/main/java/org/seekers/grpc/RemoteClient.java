package org.seekers.grpc;

import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.grpc.RemoteControlGrpc.RemoteControlBlockingStub;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class RemoteClient {
	private static final Logger logger = Logger.getLogger(RemoteClient.class.getName());

	private final ManagedChannel channel;
	private final RemoteControlBlockingStub blockingStub;

	public RemoteClient() {
		channel = ManagedChannelBuilder.forTarget("localhost:7777").usePlaintext().build();
		blockingStub = RemoteControlGrpc.newBlockingStub(channel);
		logger.info("Client started");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					RemoteClient.this.stop();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		});
	}

	public void stop() throws Exception {
		if (channel != null) {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
			System.err.println("client shutdown");
		}
	}

	public boolean isRunning() {
		ConnectivityState state = channel.getState(false);
		if(state == ConnectivityState.IDLE) {
			return true;
		}
		if (state == ConnectivityState.TRANSIENT_FAILURE | state == ConnectivityState.SHUTDOWN) {
			if (!channel.isShutdown())
				try {
					stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			return false;
		}
		return true;
	}

	public String generateToken(String username) {
		return Base64.getEncoder().encodeToString(username.getBytes());
	}

	public SessionReply getSessionStatus(String token) {
		if (!isRunning()) {
			return SessionReply.newBuilder().build();
		}
		return blockingStub.sessionStatus(SessionRequest.newBuilder().setToken(token).build());
	}

	public SessionReply getSessionStatus() {
		if (!isRunning()) {
			return SessionReply.newBuilder().build();
		}
		return blockingStub.sessionStatus(SessionRequest.newBuilder().build());
	}

	public EntityReply getEntityStatus() {
		if (!isRunning()) {
			return EntityReply.newBuilder().build();
		}
		return blockingStub.entityStatus(EntityRequest.newBuilder().build());
	}

	public PlayerReply getPlayerStatus() {
		if (!isRunning()) {
			return PlayerReply.newBuilder().build();
		}
		return blockingStub.playerStatus(PlayerRequest.newBuilder().build());
	}

	public WorldReply getWorldStatus() {
		if (!isRunning()) {
			return WorldReply.newBuilder().build();
		}
		return blockingStub.worldStatus(WorldRequest.newBuilder().build());
	}

	public CommandReply setCommand(String token, String id, Vector target, Magnet magnet) {
		if (!isRunning()) {
			return CommandReply.newBuilder().build();
		}
		return blockingStub
				.commandUnit(CommandRequest.newBuilder().setToken(token).setTarget(target).setMagnet(magnet).build());
	}
}
