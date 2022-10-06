package org.seekers.world;

import org.seekers.grpc.GoalStatus;
import org.seekers.grpc.PhysicalStatus;

import javafx.geometry.Point2D;

public class Goal extends Physical {
	private Camp camp;

	private double scoringTime = 150;
	private double timeOwned = 0;

	public Goal(World world, Point2D position) {
		super(world, position, Point2D.ZERO);

		getWorld().getGoals().put(toString(), this);
		setMass(0.5);
		setRange(6);
	}

	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		adopt(deltaT);
	}

	@Override
	protected void accelerate(double deltaT) {
		Point2D force = Point2D.ZERO;
		for (Seeker seeker : getWorld().getSeekers().values()) {
			force = force.add(seeker.getMagneticForce(getPosition()));
		}
		setAcceleration(force.multiply(deltaT));
	}

	private void adopt(double deltaT) {
		for (Camp camp : getWorld().getCamps().values()) {
			if (camp.contains(getPosition())) {
				if (this.camp == camp) {
					timeOwned += deltaT;
					if (timeOwned >= scoringTime) {
						score(camp.getPlayer());
						return;
					}
				} else {
					this.camp = camp;
					timeOwned = 0;
				}
			}
		}
	}

	private void score(Player player) {
		player.putUp();
		setPosition(getWorld().getRandomPosition());
	}

	@Override
	public Object asBuilder() {
		return GoalStatus.newBuilder().setSuper((PhysicalStatus) super.asBuilder())
				.setCampId((camp != null) ? camp.toString() : "").setTimeOwned(timeOwned).build();
	}
}
