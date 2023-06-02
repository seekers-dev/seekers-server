package org.seekers.grpc;

import org.seekers.grpc.net.StatusResponse;

import com.google.protobuf.Message;

import io.scvis.proto.HashTable;
import io.scvis.proto.Reference;
import io.scvis.proto.Table;

/**
 * The SeekersStoreHelper class is a utility class that provides functionality
 * for storing and updating data associated with the Seekers game. It implements
 * the Reference interface for updating the stored data.
 */
public class SeekersStoreHelper implements Reference<StatusResponse> {
	private final Table<String, String, Message> container = new HashTable<>();

	/**
	 * Updates the stored data based on the associated StatusResponse.
	 *
	 * @param associated The associated StatusResponse containing the data to
	 *                   update.
	 */
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

	/**
	 * Stores the specified value with the given type and id in the container.
	 *
	 * @param type  The type of the value.
	 * @param id    The id of the value.
	 * @param value The value to store.
	 * @param <T>   The type of the value.
	 */
	public <T extends Message> void store(String type, String id, T value) {
		container.set(type, id, value);
	}

	/**
	 * Retrieves the container holding the stored data.
	 *
	 * @return The container.
	 */
	public Table<String, String, Message> getContainer() {
		return container;
	}
}
