package com.seekers.grpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.karlz.grpc.bounds.Vector;
import com.karlz.grpc.exchange.HostingGrpc;
import com.karlz.grpc.exchange.HostingGrpc.HostingBlockingStub;
import com.karlz.grpc.exchange.JoinRequest;
import com.karlz.grpc.exchange.JoinResponse;
import com.seekers.graphic.Game;
import com.seekers.grpc.game.CommandRequest;
import com.seekers.grpc.game.CommandResponse;
import com.seekers.grpc.game.PropertiesRequest;
import com.seekers.grpc.game.PropertiesResponse;
import com.seekers.grpc.game.SeekersGrpc;
import com.seekers.grpc.game.SeekersGrpc.SeekersBlockingStub;
import com.seekers.grpc.game.StatusRequest;
import com.seekers.grpc.game.StatusResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.scene.layout.BorderPane;

public class SeekersClient {
	private static final Logger logger = Logger.getLogger(SeekersClient.class.getName());

	private final ManagedChannel channel;
	private final SeekersBlockingStub seekersBlockingStub;
	private final HostingBlockingStub hostingBlockingStub;

	public SeekersClient() {
		channel = ManagedChannelBuilder.forAddress("localhost", 7777).usePlaintext().build();
		seekersBlockingStub = SeekersGrpc.newBlockingStub(channel);
		hostingBlockingStub = HostingGrpc.newBlockingStub(channel);
		start();
	}

	public void start() {
		game.start(this);
		logger.info("Client started");
	}

	public void stop() throws Exception {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		logger.info("Client shutdown");
	}

	private final Game game = new Game(new BorderPane());

	private String token = "", playerId = "";

	public void join(String name, String color) {
		try {
			JoinResponse reply = hostingBlockingStub
					.join(JoinRequest.newBuilder().putAllDetails(Map.of("name", name, "color", color)).build());
			token = reply.getToken();
			playerId = reply.getPlayerId();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public PropertiesResponse getProperties() {
		try {
			return seekersBlockingStub.properties(PropertiesRequest.newBuilder().build());
		} catch (Exception ex) {
			return PropertiesResponse.newBuilder().build();
		}
	}

	public StatusResponse getStatus() {
		try {
			return seekersBlockingStub.status(StatusRequest.newBuilder().setToken(token).build());
		} catch (Exception ex) {
			return StatusResponse.newBuilder().build();
		}
	}

	public CommandResponse setCommand(String id, Vector target, double magnet) {
		try {
			return seekersBlockingStub.command(CommandRequest.newBuilder().setToken(token).setSeekerId(id)
					.setTarget(target).setMagnet(magnet).build());
		} catch (Exception ex) {
			return CommandResponse.newBuilder().build();
		}
	}

	public String getToken() {
		return token;
	}

	public String getPlayerId() {
		return playerId;
	}

	public Game getGame() {
		return game;
	}
}
