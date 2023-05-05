package com.seekers.game;

import com.seekers.grpc.SeekersDispatchHelper;

import io.scvis.geometry.Vector2D;

public class Goal extends Physical {
	private Camp capture;

	private double scoringTime;
	private double timeOwned = 0;

	public Goal(Game game, Vector2D position) {
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
		Vector2D force = Vector2D.ZERO;
		for (Seeker seeker : getGame().getSeekers()) {
			force = force.add(seeker.getMagneticForce(getPosition()));
		}
		setAcceleration(force.multiply(deltaT));
	}

	private void adopt(double deltaT) {
		for (Camp camp : getGame().getCamps()) {
			if (camp.contains(getPosition())) {
				if (this.capture == camp) {
					timeOwned += deltaT;
					if (timeOwned >= scoringTime) {
						score(camp.getPlayer());
						return;
					}
				} else {
					this.capture = camp;
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
		capture = null;
		timeOwned = 0;
	}

	@Override
	public com.seekers.grpc.game.Goal associated() {
		return com.seekers.grpc.game.Goal.newBuilder().setSuper((com.seekers.grpc.game.Physical) super.associated())
				.setCampId((capture != null) ? capture.getId() : "").setTimeOwned(timeOwned).build();
	}

	@Override
	public void changed() {
		for (SeekersDispatchHelper helper : getGame().getHelpers().values())
			helper.getGoals().add(this);
	}
}
