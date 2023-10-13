package org.seekers.game;

import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Base class for all animations.
 *
 * @author karlz
 */
public abstract class Animation extends Pane implements Entity, Destroyable {

	private final @Nonnull Game game;

	protected Animation(@Nonnull Game game) {
		this.game = game;
		game.getAnimations().add(this);
		game.getEntities().add(this);
	}

	/**
	 * Processes the animation.
	 */
	protected abstract void animate();

	@OverridingMethodsMustInvokeSuper
	@Override
	public void update() {
		animate();
	}

	@Override
	public void destroy() {
		game.getAnimations().remove(this);
		game.getEntities().remove(this);
	}

	@Nonnull
	public Game getGame() {
		return game;
	}
}
