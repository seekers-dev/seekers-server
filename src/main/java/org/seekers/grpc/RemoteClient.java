package org.seekers.grpc;

import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.grpc.RemoteControlGrpc.RemoteControlBlockingStub;

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
			channel.shutdown().awaitTermination(30, TimeUnit.SECONDS);
			System.err.println("Server shutdown");
		}
	}

	public String generateToken(String username) {
		return Base64.getEncoder().encodeToString(username.getBytes());
	}

	public SessionReply getSessionStatus(String token) {
		return blockingStub.sessionStatus(SessionRequest.newBuilder().setToken(token).build());
	}

	public SessionReply getSessionStatus() {
		return blockingStub.sessionStatus(SessionRequest.newBuilder().build());
	}

	public EntityReply getEntityStatus() {
		return blockingStub.entityStatus(EntityRequest.newBuilder().build());
	}

	public PlayerReply getPlayerStatus() {
		return blockingStub.playerStatus(PlayerRequest.newBuilder().build());
	}

	public WorldReply getWorldStatus() {
		return blockingStub.worldStatus(WorldRequest.newBuilder().build());
	}

	public CommandReply setCommand(String token, String id, Vector target, Magnet magnet) {
		return blockingStub
				.commandUnit(CommandRequest.newBuilder().setToken(token).setTarget(target).setMagnet(magnet).build());
	}
}
