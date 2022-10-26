package org.seekers.game;

public interface Entity {
	public void update(double deltaT);

	public default void tick() {
		update(1);
	};
}
