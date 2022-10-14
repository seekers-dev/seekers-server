package org.seekers.world;

import java.util.Collection;

import org.seekers.grpc.Buildable;
import org.seekers.grpc.PhysicalStatus;
import org.seekers.grpc.SeekerStatus;

import javafx.geometry.Point2D;

public class Seeker extends Physical {
	private final Player player;
	private final Magnet magnet = new Magnet();

	private Point2D target = getPosition();

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

	static boolean auto = true;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		if (auto) {
			Goal goal = (Goal) getWorld().getNearestPhysicalOf(getPosition(),
					(Collection) getWorld().getGoals().values());
			if (getWorld().getTorusDistance(getPosition(), goal.getPosition()) > 20) {
				setTarget(goal.getPosition());
				getMagnet().disable();
			} else {
				getMagnet().setAttractive();
				setTarget(getPlayer().getCamp().getPosition());
			}
		}
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
			if (getMagnet().isActivated()) {
				disable();
				if (collision.getMagnet().isActivated())
					collision.disable();
			} else if (collision.getMagnet().isActivated()) {
				collision.disable();
			} else {
				disable();
				collision.disable();
			}
		}

		super.collision(another, minDistance);
	}

	public Point2D getMagneticForce(Point2D p) {
		double r = getWorld().getTorusDistance(getPosition(), p) / getWorld().getDiameter() * 10;
		Point2D d = getWorld().getTorusDirection(getPosition(), p);
		return (isDisabled()) ? Point2D.ZERO
				: d.multiply(-magnet.strength * ((r < 1) ? Math.exp(1 / (Math.pow(r, 2) - 1)) : 0));
	}

	@Override
	public double getThrust() {
		return super.getThrust() * ((magnet.isActivated()) ? magnetSlowdown : 1);
	}

	public Player getPlayer() {
		return player;
	}

	public Magnet getMagnet() {
		return magnet;
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

	public class Magnet implements Buildable {
		private int strength;

		public void setMode(org.seekers.grpc.Magnet mode) {
			switch (mode) {
			case DISABLED: {
				disable();
			}
			case ATTRACTIVE: {
				setAttractive();
			}
			case REPULSIVE: {
				setRepulsive();
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + mode);
			}
		}

		public void disable() {
			strength = 0;
		}

		public boolean isActivated() {
			return strength != 0;
		}

		public void setAttractive() {
			strength = 1;
		}

		public void setRepulsive() {
			strength = -8;
		}

		@Override
		public Object asBuilder() {
			switch (strength) {
			case 0: {
				return org.seekers.grpc.Magnet.DISABLED;
			}
			case 1: {
				return org.seekers.grpc.Magnet.ATTRACTIVE;
			}
			case -8: {
				return org.seekers.grpc.Magnet.REPULSIVE;
			}
			default:
				return org.seekers.grpc.Magnet.UNRECOGNIZED;
			}
		}
	}

	@Override
	public Object asBuilder() {
		return SeekerStatus.newBuilder().setSuper((PhysicalStatus) super.asBuilder()).setPlayerId(player.toString())
				.setMagnet((org.seekers.grpc.Magnet) magnet.asBuilder()).setTarget(asVector(target))
				.setDisableCounter(disabledCounter).build();
	}
}
