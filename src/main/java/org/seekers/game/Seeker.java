package org.seekers.game;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * The Seeker class represents a seeker in the game.
 * 
 * @author karlz
 */
public class Seeker extends Physical {
	@Nonnull
	private final Player player;
	@Nonnull
	private Vector2D target = getPosition();

	private double magnet = 0;
	private double magnetSlowdown = SeekerProperties.getDefault().getSeekerMagnetSlowdown();
	private double disabledTime = SeekerProperties.getDefault().getSeekerDisabledTime();
	private double disabledCounter = 0;
	@Nonnull
	private List<Circle> indicators = new ArrayList<>();

	/**
	 * Constructs a new instance of the Seeker class.
	 *
	 * @param player   The Player object associated with the Seeker.
	 * @param position The position of the Seeker.
	 */
	public Seeker(@Nonnull Player player, @Nullable Vector2D position) {
		super(player.getGame(), position);
		this.player = player;
		setRange(SeekerProperties.getDefault().getSeekerRadius());
		getObject().setFill(player.getColor());
		for (int i = 1; i < 3; i++) {
			Circle indicator = new Circle(getRange() + i * 0.25 * getAnimationRange());
			indicator.setFill(Color.TRANSPARENT);
			indicator.setStroke(player.getColor());
			indicator.setStrokeWidth(2);
			getIndicators().add(indicator);
		}
		getMirror().getReflection().getChildren().addAll(getIndicators());

		addInvalidationListener(e -> {
			if (isDisabled()) {
				getObject().setFill(disabled);
			} else {
				getObject().setFill(activated);
			}
			if (getMagnet() != 0) {
				getIndicators().forEach(c -> c.setVisible(true));
			} else {
				getIndicators().forEach(c -> c.setVisible(false));
			}
		});

		player.getSeekers().put(getId(), this);
		getGame().getSeekers().put(getId(), this);
	}

	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		animate(deltaT);
		if (isDisabled()) {
			disabledCounter = Math.max(disabledCounter - deltaT, 0);
		}
	}

	@Override
	public void accelerate(double deltaT) {
		if (!isDisabled()) {
			setAcceleration(getGame().getTorusDirection(getPosition(), getTarget()).multiply(deltaT));
		} else {
			setAcceleration(Vector2D.ZERO);
		}
	}

	@Override
	public void collision(Physical another, double minDistance) {
		if (another instanceof Seeker) {
			Seeker collision = (Seeker) another;
			if (magnet != 0) {
				disable();
				if (collision.magnet != 0)
					collision.disable();
			} else if (collision.magnet != 0) {
				collision.disable();
			} else {
				disable();
				collision.disable();
			}
		}

		super.collision(another, minDistance);
	}

	/**
	 * Animates the Seeker based on the time passed.
	 *
	 * @param deltaT The time passed since the last animation.
	 */
	public void animate(double deltaT) {
		for (Circle indicator : getIndicators()) {
			indicator.setRadius(getRange()
					+ (indicator.getRadius() - Math.signum(magnet) * (getRange() - deltaT)) % getAnimationRange());
		}
	}

	/**
	 * Sets the automatic commands for the Seeker.
	 */
	public void setAutoCommands() {
		Goal goal = (Goal) getGame().getNearestPhysicalOf(getPosition(), getGame().getGoals().values());
		if (getGame().getTorusDistance(getPosition(), goal.getPosition()) > 30) {
			setTarget(goal.getPosition());
			setMagnet(0);
		} else {
			setTarget(getPlayer().getCamp().getPosition());
			setMagnet(1);
		}
	}

	private static final double ANIMATION_RANGE = 18.0;

	/**
	 * Returns the animation range of the Seeker.
	 *
	 * @return The animation range of the Seeker.
	 */
	public double getAnimationRange() {
		return ANIMATION_RANGE;
	}

	/**
	 * Calculates the magnetic force between the Seeker and a given position.
	 *
	 * @param p The position to calculate the magnetic force with.
	 * @return The magnetic force vector.
	 */
	public Vector2D getMagneticForce(Vector2D p) {
		double r = getGame().getTorusDistance(getPosition(), p) / getGame().getDiameter() * 10;
		Vector2D d = getGame().getTorusDirection(getPosition(), p);
		double s = (r < 1) ? Math.exp(1 / (Math.pow(r, 2) - 1)) : 0;
		return (isDisabled()) ? Vector2D.ZERO : d.multiply(-getMagnet() * s);
	}

	@Nonnull
	public List<Circle> getIndicators() {
		return indicators;
	}

	/**
	 * Returns the thrust of the Seeker, taking into account the magnet's effect.
	 *
	 * @return The thrust of the Seeker.
	 */
	@Override
	public double getThrust() {
		return super.getThrust() * (magnet != 0 ? magnetSlowdown : 1);
	}

	/**
	 * Returns the Player object associated with the Seeker.
	 *
	 * @return The Player object associated with the Seeker.
	 */
	@Nonnull
	public Player getPlayer() {
		return player;
	}

	/**
	 * Returns the magnet value of the Seeker.
	 *
	 * @return The magnet value of the Seeker.
	 */
	public double getMagnet() {
		return magnet;
	}

	/**
	 * Sets the magnet value of the Seeker.
	 *
	 * @param magnet The magnet value to set.
	 */
	public void setMagnet(double magnet) {
		if (!isDisabled()) {
			this.magnet = Math.max(Math.min(magnet, 1), -8);
			invalidated();
		}
	}

	/**
	 * Disables the Seeker.
	 */
	public void disable() {
		if (!isDisabled()) {
			disabledCounter = disabledTime;
			setMagnet(0.0);
			invalidated();
		}
	}

	/**
	 * Checks if the Seeker is disabled.
	 *
	 * @return True if the Seeker is disabled, false otherwise.
	 */
	public boolean isDisabled() {
		return disabledCounter > 0;
	}

	/**
	 * Returns the target position of the Seeker.
	 *
	 * @return The target position of the Seeker.
	 */
	@Nonnull
	public Vector2D getTarget() {
		return target;
	}

	/**
	 * Sets the target position of the Seeker.
	 *
	 * @param target The target position to set.
	 */
	public void setTarget(@Nonnull Vector2D target) {
		this.target = target;
	}

	private Color activated;
	private Color disabled;

	/**
	 * Returns the color of the Seeker.
	 *
	 * @return The color of the Seeker.
	 */
	public Color getColor() {
		return activated;
	}

	/**
	 * Sets the color of the Seeker.
	 *
	 * @param color The color to set.
	 */
	public void setColor(Color color) {
		this.activated = color;
		this.disabled = color.darker().darker();
		for (Circle circle : getIndicators()) {
			circle.setStroke(color);
		}
		invalidated();
	}

	@Override
	public org.seekers.grpc.game.Seeker associated() {
		return org.seekers.grpc.game.Seeker.newBuilder().setSuper((org.seekers.grpc.game.Physical) super.associated())
				.setPlayerId(player.getId()).setMagnet(magnet).setTarget(TorusMap.toMessage(target))
				.setDisableCounter(disabledCounter).build();
	}
}
