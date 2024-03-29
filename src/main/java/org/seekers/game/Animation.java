package org.seekers.game;

import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;

/**
 * Base class for all animations.
 *
 * @author karlz
 */
public abstract class Animation extends Pane implements Entity {

	private final @Nonnull Game game;

	protected Animation(@Nonnull Game game) {
		this.game = game;
		game.getEntities().add(this);
		game.getFront().getChildren().add(this);
	}

	public void destroy() {
		game.getFront().getChildren().remove(this);
		game.getEntities().remove(this);
	}

	@Nonnull
	public Game getGame() {
		return game;
	}
}
