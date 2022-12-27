package org.seekers.grpc;

import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.grpc.SeekersGrpc.SeekersBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class SeekersClient {
	private static final Logger logger = Logger.getLogger(SeekersClient.class.getName());

	private final ManagedChannel channel;
	private final SeekersBlockingStub blockingStub;

	public SeekersClient() {
		channel = ManagedChannelBuilder.forAddress("localhost", 7777).usePlaintext().build();
		blockingStub = SeekersGrpc.newBlockingStub(channel);
		logger.info("Client started");
	}

	public void stop() throws Exception {
		if (channel != null) {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
			logger.info("Client shutdown");
		}
	}

	public String generateToken(String username) {
		return Base64.getEncoder().encodeToString(username.getBytes());
	}

	public SessionReply getSessionStatus(String token) {
		if (token.isBlank()) {
			throw new UnsupportedOperationException("Can not use a blank string as token");
		}
		try {
			return blockingStub.joinSession(SessionRequest.newBuilder().setToken(token).build());
		} catch (Exception ex) {
			return SessionReply.newBuilder().build();
		}
	}

	public PropertiesReply getProperties() {
		try {
			return blockingStub.propertiesInfo(PropertiesRequest.newBuilder().build());
		} catch (Exception ex) {
			return PropertiesReply.newBuilder().build();
		}
	}

	public EntityReply getEntityStatus() {
		try {
			return blockingStub.entityStatus(EntityRequest.newBuilder().build());
		} catch (Exception ex) {
			return EntityReply.newBuilder().build();
		}
	}

	public PlayerReply getPlayerStatus() {
		try {
			return blockingStub.playerStatus(PlayerRequest.newBuilder().build());
		} catch (Exception ex) {
			return PlayerReply.newBuilder().build();
		}
	}

	public CommandReply setCommand(String token, String id, Vector target, double magnet) {
		try {
			return blockingStub.commandUnit(
					CommandRequest.newBuilder().setToken(token).setId(id).setTarget(target).setMagnet(magnet).build());
		} catch (Exception ex) {
			return CommandReply.newBuilder().build();
		}
	}
}
