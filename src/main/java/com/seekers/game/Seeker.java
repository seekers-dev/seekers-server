package com.seekers.game;

import java.util.Collection;

import com.karlz.bounds.Vector;
import com.karlz.exchange.Corresponding;
import com.seekers.grpc.SeekersDispatchHelper;

public class Seeker extends Physical {
	private final Player player;

	private Vector target = getPosition();

	private double magnet = 0;
	private double magnetSlowdown;
	private double disabledTime;
	private double disabledCounter = 0;

	public Seeker(Player player, Vector position) {
		super(player.getGame(), position);
		this.player = player;
		magnetSlowdown = Double.valueOf(player.getGame().getProperties().getProperty("seeker.magnet-slowdown"));
		disabledTime = Double.valueOf(player.getGame().getProperties().getProperty("seeker.disabled-time"));
		setMass(Double.valueOf(player.getGame().getProperties().getProperty("seeker.mass")));
		setRange(Double.valueOf(player.getGame().getProperties().getProperty("seeker.radius")));
		player.getSeekers().put(getId(), this);
		getGame().getSeekers().add(this);
		changed();
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
			setAcceleration(Vector.ZERO);
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
				(Collection<Physical>) (Collection<?>) getGame().getGoals());
		if (getGame().getTorusDistance(getPosition(), goal.getPosition()) > 20) {
			setTarget(goal.getPosition());
			setMagnet(0);
		} else {
			setTarget(getPlayer().getCamp().getPosition());
			setMagnet(1);
		}
	}

	public Vector getMagneticForce(Vector p) {
		double r = getGame().getTorusDistance(getPosition(), p) / getGame().getDiameter() * 10;
		Vector d = getGame().getTorusDirection(getPosition(), p);
		return (isDisabled()) ? Vector.ZERO
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
		changed();
	}

	public void disable() {
		if (!isDisabled()) {
			disabledCounter = disabledTime;
			changed();
		}
	}

	public boolean isDisabled() {
		return disabledCounter > 0;
	}

	public Vector getTarget() {
		return target;
	}

	public void setTarget(Vector target) {
		this.target = target;
		changed();
	}

	@Override
	public com.seekers.grpc.game.Seeker associated() {
		return com.seekers.grpc.game.Seeker.newBuilder().setSuper((com.seekers.grpc.game.Physical) super.associated())
				.setPlayerId(player.getId()).setMagnet(magnet).setTarget(Corresponding.transform(target))
				.setDisableCounter(disabledCounter).build();
	}

	@Override
	public void changed() {
		for (SeekersDispatchHelper helper : getGame().getHelpers().values())
			helper.getSeekers().add(this);
	}
}
