package com.seekers.game;

import com.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;
import javafx.scene.paint.Color;

public class Goal extends Physical {
	private Camp capture;

	private double scoringTime = SeekerProperties.getDefault().getGoalScoringTime();
	private double timeOwned = 0;

	public Goal(Game game, Vector2D position) {
		super(game, position);
		addInvalidationListener(e -> getGame().getHelpers().values().forEach(h -> h.getGoals().add(this)));

		getMirror().getReflection().setFill(Color.WHITESMOKE);
		setMass(SeekerProperties.getDefault().getGoalMass());
		setRange(SeekerProperties.getDefault().getGoalRadius());
		getGame().getGoals().put(getId(), this);
	}

	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		adopt(deltaT);
	}

	@Override
	public void accelerate(double deltaT) {
		Vector2D force = Vector2D.ZERO;
		for (Seeker seeker : getGame().getSeekers().values()) {
			force = force.add(seeker.getMagneticForce(getPosition()));
		}
		setAcceleration(force.multiply(deltaT));
	}

	private void adopt(double deltaT) {
		for (Camp camp : getGame().getCamps().values()) {
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
}
