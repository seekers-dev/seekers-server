package org.seekers.game;

import org.seekers.grpc.SeekersDispatchHelper;
import org.seekers.grpc.StatusReply;

import javafx.geometry.Point2D;

public class Goal extends Physical {
	private Camp camp;

	private double scoringTime;
	private double timeOwned = 0;

	public Goal(Game game, Point2D position) {
		super(game, position);
		scoringTime = Double.valueOf(game.getProperties().getProperty("goal.scoring-time"));

		getGame().getGoals().add(this);
		setMass(Double.valueOf(game.getProperties().getProperty("goal.mass")));
		setRange(Double.valueOf(game.getProperties().getProperty("goal.radius")));
	}

	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		adopt(deltaT);
	}

	@Override
	protected void accelerate(double deltaT) {
		Point2D force = Point2D.ZERO;
		for (Seeker seeker : getGame().getSeekers()) {
			force = force.add(seeker.getMagneticForce(getPosition()));
		}
		setAcceleration(force.multiply(deltaT));
	}

	private void adopt(double deltaT) {
		for (Camp camp : getGame().getCamps()) {
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
		reset();
	}

	private void reset() {
		setPosition(getGame().getRandomPosition());
		camp = null;
		timeOwned = 0;
	}

	@Override
	public Object associated() {
		return StatusReply.Goal.newBuilder().setSuper((StatusReply.Physical) super.associated())
				.setCampId((camp != null) ? camp.getId() : "").setTimeOwned(timeOwned).build();
	}

	@Override
	public void changed() {
		for (SeekersDispatchHelper helper : getGame().getHelpers().values())
			helper.getGoals().add(this);
	}
}
