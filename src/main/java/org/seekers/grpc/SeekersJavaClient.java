package org.seekers.grpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
import io.scvis.grpc.game.HostingGrpc;
import io.scvis.grpc.game.HostingGrpc.HostingBlockingStub;
import io.scvis.grpc.game.JoinRequest;
import io.scvis.grpc.game.JoinResponse;
import io.scvis.grpc.geometry.Vector2D;
import javafx.scene.paint.Color;

public class SeekersJavaClient {
	private static final Logger logger = Logger.getLogger(SeekersJavaClient.class.getName());

	private final ManagedChannel channel;
	private final SeekersBlockingStub seekersBlockingStub;
	private final HostingBlockingStub hostingBlockingStub;

	private final SeekersStoreHelper helper = new SeekersStoreHelper();

	public SeekersJavaClient() {
		channel = ManagedChannelBuilder.forAddress("localhost", 7777).usePlaintext().build();
		seekersBlockingStub = SeekersGrpc.newBlockingStub(channel);
		hostingBlockingStub = HostingGrpc.newBlockingStub(channel);
		start();
	}

	public void start() {
		join(toString(), Color.color(Math.random(), Math.random(), Math.random()).toString());
		logger.info("Client started");
	}

	public void stop() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		logger.info("Client shutdown");
	}

	private String token = "";
	private String playerId = "";

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

	public SeekersStoreHelper getHelper() {
		return helper;
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
			StatusResponse response = seekersBlockingStub.status(StatusRequest.newBuilder().setToken(token).build());
			helper.update(response);
			return response;
		} catch (Exception ex) {
			return StatusResponse.newBuilder().build();
		}
	}

	public CommandResponse setCommand(String id, Vector2D target, double magnet) {
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
}
