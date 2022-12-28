package org.seekers.grpc;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.seekers.graphic.Game;
import org.seekers.grpc.SeekersGrpc.SeekersBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.scene.layout.BorderPane;

public class SeekersClient {
	private static final Logger logger = Logger.getLogger(SeekersClient.class.getName());

	private final ManagedChannel channel;
	private final SeekersBlockingStub blockingStub;

	public SeekersClient() {
		channel = ManagedChannelBuilder.forAddress("localhost", 7777).usePlaintext().build();
		blockingStub = SeekersGrpc.newBlockingStub(channel);
		logger.info("Client started");
		game.start(this);
	}

	private final Game game = new Game(new BorderPane());

	public void stop() throws Exception {
		if (channel != null) {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
			logger.info("Client shutdown");
		}
	}

	public Game getGame() {
		return game;
	}

	private JoinReply reply;

	public void join(String name, String color) {
		try {
			reply = blockingStub.join(JoinRequest.newBuilder().setName(name).setColor(color).build());
		} catch (Exception ex) {
			ex.printStackTrace();
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
			return blockingStub.status(StatusRequest.newBuilder().setToken(reply.getToken()).build());
		} catch (Exception ex) {
			return StatusReply.newBuilder().build();
		}
	}

	public CommandReply setCommand(String id, Vector target, double magnet) {
		try {
			return blockingStub.command(CommandRequest.newBuilder().setToken(reply.getToken()).setSeekerId(id)
					.setTarget(target).setMagnet(magnet).build());
		} catch (Exception ex) {
			return CommandReply.newBuilder().build();
		}
	}

	public JoinReply getJoinReply() {
		return reply;
	}
}
