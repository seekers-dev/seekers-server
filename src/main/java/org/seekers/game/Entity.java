package org.seekers.game;

public interface Entity {
	void update(double deltaT);

	default void tick() {
		update(1);
	};
}
