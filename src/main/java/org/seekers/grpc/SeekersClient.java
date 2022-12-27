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

	public JoinReply getJoined(String name, String color) {
		try {
			return blockingStub.join(JoinRequest.newBuilder().setName(name).setColor(color).build());
		} catch (Exception ex) {
			return JoinReply.newBuilder().build();
		}
	}

	public PropertiesReply getProperties() {
		try {
			return blockingStub.properties(PropertiesRequest.newBuilder().build());
		} catch (Exception ex) {
			return PropertiesReply.newBuilder().build();
		}
	}

	public StatusReply getStatus() {
		try {
			return blockingStub.status(StatusRequest.newBuilder().build());
		} catch (Exception ex) {
			return StatusReply.newBuilder().build();
		}
	}

	public CommandReply setCommand(String token, String id, Vector target, double magnet) {
		try {
			return blockingStub.command(CommandRequest.newBuilder().setToken(token).setSeekerId(id).setTarget(target)
					.setMagnet(magnet).build());
		} catch (Exception ex) {
			return CommandReply.newBuilder().build();
		}
	}
}
