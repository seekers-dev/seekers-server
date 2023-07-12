package org.seekers.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.seekers.grpc.SeekersProperties;

import io.scvis.geometry.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * A goal is a physical object that can be adopted by a camp and used for
 * scoring. It keeps track of the time owned by a camp and triggers scoring when
 * the required time is reached.
 * 
 * @author karlz
 */
public class Goal extends Physical {
	@Nullable
	private Camp capture;

	private double scoringTime = SeekersProperties.getDefault().getGoalScoringTime();
	private double timeOwned = 0;

	/**
	 * Constructs a new instance of the Goal class.
	 *
	 * @param game     The Game object associated with the Goal object.
	 * @param position The initial position of the Goal object.
	 */
	public Goal(@Nonnull Game game, @Nullable Vector2D position) {
		super(game, position);
		getObject().setFill(Color.WHITE);
		setMass(SeekersProperties.getDefault().getGoalMass());
		setRange(SeekersProperties.getDefault().getGoalRadius());
		getGame().getGoals().add(this);
	}

	@Override
	public void update(double deltaT) {
		super.update(deltaT);
		adopt(deltaT);
	}

	@Override
	public void accelerate(double deltaT) {
		Vector2D force = Vector2D.ZERO;
		for (int index = 0, size = getGame().getSeekers().size(); index < size; index++) {
			Seeker seeker = getGame().getSeekers().get(index);
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
		for (Camp camp : getGame().getCamps()) {
			if (camp.contains(getPosition())) {
				if (this.capture == camp) {
					setTimeOwned(getTimeOwned() + deltaT);
					if (timeOwned >= scoringTime) {
						score(camp.getPlayer());
						return;
					}
				} else {
					this.capture = camp;
					setTimeOwned(0);
				}
			}
		}
	}

	public class GoalAnimation extends Animation {

		private final Circle wave = new Circle(0);

		public GoalAnimation(Game game) {
			super(game);
			getChildren().add(wave);
			setLayoutX(getPosition().getX());
			setLayoutY(getPosition().getY());
			final Camp checked = capture;
			Color color = checked != null ? checked.getPlayer().getColor() : Color.WHITE;
			wave.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.25));
			wave.setStroke(color);
			wave.setStrokeWidth(2);
		}

		private static final double ANIMATION_RANGE = 50.0;

		@Override
		protected void animate(double deltaT) {
			var next = wave.getRadius() + deltaT * 0.75;
			if (next < ANIMATION_RANGE) {
				wave.setRadius(next);
				wave.setStrokeWidth(1 + next / ANIMATION_RANGE);
			} else {
				destroy();
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
		new GoalAnimation(getGame());
		reset();
	}

	/**
	 * Resets the state of the Goal object.
	 */
	private void reset() {
		setPosition(getGame().getRandomPosition());
		capture = null;
		setTimeOwned(0);
	}

	public void setTimeOwned(double timeOwned) {
		this.timeOwned = timeOwned;
		if (timeOwned == 0) {
			getObject().setFill(Color.WHITE);
		} else {
			final Camp checked = this.capture;
			if (checked != null) {
				Color color = checked.getPlayer().getColor();
				getObject().setFill(Color.color(1 + (color.getRed() - 1) * timeOwned / scoringTime,
						1 + (color.getGreen() - 1) * timeOwned / scoringTime,
						1 + (color.getBlue() - 1) * timeOwned / scoringTime));
			}
		}
	}

	public double getTimeOwned() {
		return timeOwned;
	}

	@Override
	public org.seekers.grpc.game.Goal associated() {
		final Camp checked = this.capture;
		return org.seekers.grpc.game.Goal.newBuilder().setSuper((org.seekers.grpc.game.Physical) super.associated())
				.setCampId((checked != null) ? checked.getId() : "").setTimeOwned(timeOwned).build();
	}
}
