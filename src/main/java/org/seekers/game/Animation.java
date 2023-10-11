package org.seekers.game;

import javafx.scene.layout.Pane;

public abstract class Animation extends Pane implements Entity, Destroyable {

	private final Game game;

	protected Animation(Game game) {
		this.game = game;
		game.getAnimations().add(this);
		game.getEntities().add(this);
	}

	protected abstract void animate();

	@Override
	public void update() {
		animate();
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
