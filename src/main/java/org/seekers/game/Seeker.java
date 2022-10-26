package org.seekers.game;

import java.util.Collection;

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
		super(player.getWorld(), position);
		this.player = player;
		magnetSlowdown = Double.valueOf(player.getWorld().getProperties().getProperty("seeker.magnet-slowdown"));
		disabledTime = Double.valueOf(player.getWorld().getProperties().getProperty("seeker.disabled-time"));
		setMass(Double.valueOf(player.getWorld().getProperties().getProperty("seeker.mass")));
		setRange(Double.valueOf(player.getWorld().getProperties().getProperty("seeker.radius")));
		player.getSeekers().put(toString(), this);
		getWorld().getSeekers().put(toString(), this);
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
			setAcceleration(getWorld().getTorusDirection(getPosition(), getTarget()).multiply(deltaT));
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
		Goal goal = (Goal) getWorld().getNearestPhysicalOf(getPosition(),
				(Collection<Physical>) (Collection<?>) getWorld().getGoals().values());
		if (getWorld().getTorusDistance(getPosition(), goal.getPosition()) > 20) {
			setTarget(goal.getPosition());
			setMagnet(0);
		} else {
			setTarget(getPlayer().getCamp().getPosition());
			setMagnet(1);
		}
	}

	public Point2D getMagneticForce(Point2D p) {
		double r = getWorld().getTorusDistance(getPosition(), p) / getWorld().getDiameter() * 10;
		Point2D d = getWorld().getTorusDirection(getPosition(), p);
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
	public Object asBuilder() {
		return SeekerStatus.newBuilder().setSuper((PhysicalStatus) super.asBuilder()).setPlayerId(player.toString())
				.setMagnet(magnet).setTarget(asVector(target)).setDisableCounter(disabledCounter).build();
	}
}
