package org.seekers.game;

import java.util.Collection;

import org.seekers.grpc.Corresponding;
import org.seekers.grpc.PhysicalStatus;
import org.seekers.grpc.SeekerStatus;

import javafx.geometry.Point2D;

public class Seeker extends Physical {
	private final Player player;

	private Point2D target = getPosition();

	private double magnet = 0;
	private double magnetSlowdown;
	private double disabledTime;
	private double disabledCounter = 0;

	public Seeker(Player player, Point2D position) {
		super(player.getGame(), position);
		this.player = player;
		magnetSlowdown = Double.valueOf(player.getGame().getProperties().getProperty("seeker.magnet-slowdown"));
		disabledTime = Double.valueOf(player.getGame().getProperties().getProperty("seeker.disabled-time"));
		setMass(Double.valueOf(player.getGame().getProperties().getProperty("seeker.mass")));
		setRange(Double.valueOf(player.getGame().getProperties().getProperty("seeker.radius")));
		player.getSeekers().put(toString(), this);
		getGame().getSeekers().put(toString(), this);
	}

	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		if (isDisabled()) {
			disabledCounter = Math.max(disabledCounter - deltaT, 0);
		}
	}

	@Override
	protected void accelerate(double deltaT) {
		if (!isDisabled()) {
			setAcceleration(getGame().getTorusDirection(getPosition(), getTarget()).multiply(deltaT));
		} else {
			setAcceleration(Point2D.ZERO);
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

	public void setAutoCommands() {
		@SuppressWarnings("unchecked")
		Goal goal = (Goal) getGame().getNearestPhysicalOf(getPosition(),
				(Collection<Physical>) (Collection<?>) getGame().getGoals().values());
		if (getGame().getTorusDistance(getPosition(), goal.getPosition()) > 20) {
			setTarget(goal.getPosition());
			setMagnet(0);
		} else {
			setTarget(getPlayer().getCamp().getPosition());
			setMagnet(1);
		}
	}

	public Point2D getMagneticForce(Point2D p) {
		double r = getGame().getTorusDistance(getPosition(), p) / getGame().getDiameter() * 10;
		Point2D d = getGame().getTorusDirection(getPosition(), p);
		return (isDisabled()) ? Point2D.ZERO
				: d.multiply(-getMagnet() * ((r < 1) ? Math.exp(1 / (Math.pow(r, 2) - 1)) : 0));
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
	}

	public void disable() {
		if (!isDisabled())
			disabledCounter = disabledTime;
	}

	public boolean isDisabled() {
		return disabledCounter > 0;
	}

	public Point2D getTarget() {
		return target;
	}

	public void setTarget(Point2D target) {
		this.target = target;
	}

	@Override
	public Object associated() {
		return SeekerStatus.newBuilder().setSuper((PhysicalStatus) super.associated()).setPlayerId(player.toString())
				.setMagnet(magnet).setTarget(Corresponding.transform(target)).setDisableCounter(disabledCounter)
				.build();
	}
}
