package org.seekers.game;

import io.scvis.entity.Destroyable;
import io.scvis.entity.Entity;
import javafx.scene.layout.Pane;

public abstract class Animation extends Pane implements Entity, Destroyable {

	private final Game game;

	public Animation(Game game) {
		this.game = game;
		game.getAnimations().add(this);
		game.getEntities().add(this);
	}

	protected abstract void animate(double deltaT);

	@Override
	public void update(double deltaT) {
		animate(deltaT);
	}

	@Override
	public void destroy() {
		game.getAnimations().remove(this);
		game.getEntities().remove(this);
	}

	public Game getGame() {
		return game;
	}
}
