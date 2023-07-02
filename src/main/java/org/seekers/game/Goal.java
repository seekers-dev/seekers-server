package org.seekers.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;
import javafx.scene.paint.Color;

/**
 * 
 * @author karlz
 */
public class Goal extends Physical {
	@Nullable
	private Camp capture;

	private double scoringTime = SeekerProperties.getDefault().getGoalScoringTime();
	private double timeOwned = 0;

	/**
	 * Constructs a new instance of the Goal class.
	 *
	 * @param game     The Game object associated with the Goal object.
	 * @param position The initial position of the Goal object.
	 */
	public Goal(@Nonnull Game game, @Nullable Vector2D position) {
		super(game, position);

		getObject().setFill(Color.WHITESMOKE);
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

	/**
	 * Adopts the Goal object to a camp and checks for scoring.
	 *
	 * @param deltaT The time elapsed since the last update.
	 */
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

	/**
	 * Scores a goal for the given player and resets the Goal object.
	 *
	 * @param player The player who scored the goal.
	 */
	private void score(Player player) {
		player.putUp();
		reset();
	}

	/**
	 * Resets the state of the Goal object.
	 */
	private void reset() {
		setPosition(getGame().getRandomPosition());
		capture = null;
		timeOwned = 0;
	}

	@Override
	public org.seekers.grpc.game.Goal associated() {
		return org.seekers.grpc.game.Goal.newBuilder().setSuper((org.seekers.grpc.game.Physical) super.associated())
				.setCampId((capture != null) ? capture.getId() : "").setTimeOwned(timeOwned).build();
	}
}
