package org.seekers.grpc;

import org.seekers.grpc.net.StatusResponse;

import com.google.protobuf.Message;

import io.scvis.proto.HashTable;
import io.scvis.proto.Reference;
import io.scvis.proto.Table;

public class SeekersStoreHelper implements Reference<StatusResponse> {
	private final Table<String, String, Message> container = new HashTable<>();

	@Override
	public void update(StatusResponse associated) {
		for (org.seekers.grpc.game.Player player : associated.getPlayersList()) {
			store("Player", player.getId(), player);
		}
		for (org.seekers.grpc.game.Camp camp : associated.getCampsList()) {
			store("Camp", camp.getId(), camp);
		}
		for (org.seekers.grpc.game.Seeker seeker : associated.getSeekersList()) {
			store("Seeker", seeker.getSuper().getId(), seeker);
		}
		for (org.seekers.grpc.game.Goal goal : associated.getGoalsList()) {
			store("Goal", goal.getSuper().getId(), goal);
		}
	}

	public <T extends Message> void store(String type, String id, T value) {
		container.set(type, id, value);
	}

	public Table<String, String, Message> getContainer() {
		return container;
	}
}
