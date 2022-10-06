package org.seekers.world;

public interface Entity {
	public void update(double deltaT);

	public default void tick() {
		update(1);
	};
}
