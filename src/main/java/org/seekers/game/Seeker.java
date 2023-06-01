package org.seekers.game;

import java.util.ArrayList;
import java.util.List;

import org.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Seeker extends Physical {
	private final Player player;

	private Vector2D target = getPosition();

	private double magnet = 0;
	private double magnetSlowdown = SeekerProperties.getDefault().getSeekerMagnetSlowdown();
	private double disabledTime = SeekerProperties.getDefault().getSeekerDisabledTime();
	private double disabledCounter = 0;

	private final List<Circle> indicators = new ArrayList<>();

	public Seeker(Player player, Vector2D position) {
		super(player.getGame(), position);
		this.player = player;
		setRange(SeekerProperties.getDefault().getSeekerRadius());
		getObject().setFill(player.getColor());
		for (int i = 1; i < 4; i++) {
			Circle indicator = new Circle(getRange() + i * 4);
			indicator.setFill(Color.TRANSPARENT);
			indicator.setStroke(player.getColor());
			indicators.add(indicator);
		}
		getMirror().getReflection().getChildren().addAll(indicators);

		addInvalidationListener(e -> {
			if (isDisabled()) {
				getObject().setFill(disabled);
			} else {
				getObject().setFill(activated);
			}
			if (getMagnet() != 0) {
				indicators.forEach(c -> c.setVisible(true));
			} else {
				indicators.forEach(c -> c.setVisible(false));
			}
		});
		addInvalidationListener(e -> getGame().getHelpers().values().forEach(h -> h.getSeekers().add(this)));

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

	public void animate(double deltaT) {
		for (Circle indicator : indicators) {
			indicator.setRadius(
					(indicator.getRadius() + Math.signum(magnet) * (getRange() - deltaT)) % getAnimationRange());
		}
	}

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

	public Vector2D getMagneticForce(Vector2D p) {
		double r = getGame().getTorusDistance(getPosition(), p) / getGame().getDiameter() * 10;
		Vector2D d = getGame().getTorusDirection(getPosition(), p);
		double s = (r < 1) ? Math.exp(1 / (Math.pow(r, 2) - 1)) : 0;
		return (isDisabled()) ? Vector2D.ZERO : d.multiply(-getMagnet() * s);
	}

	@Override
	public double getThrust() {
		return super.getThrust() * (magnet != 0 ? magnetSlowdown : 1);
	}

	public Player getPlayer() {
		return player;
	}

	public double getMagnet() {
		return magnet;
	}

	public void setMagnet(double magnet) {
		this.magnet = Math.max(Math.min(magnet, 1), -8);
		invalidated();
	}

	public void disable() {
		if (!isDisabled()) {
			disabledCounter = disabledTime;
			invalidated();
		}
	}

	public boolean isDisabled() {
		return disabledCounter > 0;
	}

	public Vector2D getTarget() {
		return target;
	}

	public void setTarget(Vector2D target) {
		this.target = target;
	}

	private Color activated;
	private Color disabled;

	public Color getColor() {
		return activated;
	}

	public void setColor(Color color) {
		this.activated = color;
		this.disabled = color.darker().darker();
		invalidated();
	}

	public double getAnimationRange() {
		return getRange() + 3 * 4;
	}

	@Override
	public org.seekers.grpc.game.Seeker associated() {
		return org.seekers.grpc.game.Seeker.newBuilder().setSuper((org.seekers.grpc.game.Physical) super.associated())
				.setPlayerId(player.getId()).setMagnet(magnet).setTarget(TorusMap.toMessage(target))
				.setDisableCounter(disabledCounter).build();
	}
}
