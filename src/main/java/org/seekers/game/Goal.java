package org.seekers.game;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.ini4j.Ini;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.List;

/**
 * A goal is a physical object that can be adopted by a camp and used for
 * scoring. It keeps track of the time owned by a camp and triggers scoring when
 * the required time is reached.
 *
 * @author karlz
 */
public class Goal extends Physical<Goal.Properties> {

    public static Iterable<org.seekers.grpc.game.Goal> transform(Collection<? extends Goal> goals) {
        return goals.stream().map(Goal::associated).collect(Collectors.toList());
    }

    private @Nullable Camp capture;
    private double timeOwned = 0;

    /**
     * Constructs a new instance of the Goal class.
     *
     * @param game     The Game object associated with the Goal object.
     */
    public Goal(@Nonnull Game game, @Nonnull Properties properties) {
        super(game, properties);
        getObject().setFill(Color.WHITE);
        getGame().getGoals().add(this);
    }

    public static class Properties extends Physical.Properties {
        private static final String SECTION = "goal";

        public Properties(Ini ini) {
            super(ini, SECTION);
            scoringTime = ini.fetch(SECTION, "scoring-time", double.class);
        }

        private final double scoringTime;
    }

    @Override
    public void update() {
        super.update();
        adopt();
    }

    @Override
    public void accelerate() {
        Point2D force = Point2D.ZERO;
        for (Seeker seeker : List.copyOf(getGame().getSeekers())) {
            force = force.add(seeker.getMagneticForce(getPosition()));
        }
        setAcceleration(force);
    }

    /**
     * Adopts the Goal object to a camp and checks for scoring.
     */
    private void adopt() {
        for (Camp camp : getGame().getCamps()) {
            if (camp.contains(getPosition())) {
                if (this.capture == camp) {
                    setTimeOwned(getTimeOwned() + 1);
                    if (timeOwned >= properties.scoringTime) {
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

    public double getTimeOwned() {
        return timeOwned;
    }

    public void setTimeOwned(double timeOwned) {
        this.timeOwned = timeOwned;
        if (timeOwned == 0) {
            getObject().setFill(Color.WHITE);
        } else {
            final Camp checked = this.capture;
            if (checked != null) {
                Color color = checked.getPlayer().getColor();
                getObject().setFill(Color.color(1 + (color.getRed() - 1) * timeOwned / properties.scoringTime,
                        1 + (color.getGreen() - 1) * timeOwned / properties.scoringTime,
                        1 + (color.getBlue() - 1) * timeOwned / properties.scoringTime));
            }
        }
    }

    @Override
    public org.seekers.grpc.game.Goal associated() {
        return org.seekers.grpc.game.Goal.newBuilder().setSuper((org.seekers.grpc.game.Physical) super.associated())
                .setCampId((capture != null) ? capture.getIdentifier() : "").setTimeOwned(timeOwned).build();
    }

    public class GoalAnimation extends Animation {

        private static final double ANIMATION_RANGE = 50.0;
        private final Circle wave = new Circle(0);

        public GoalAnimation(Game game) {
            super(game);
            getChildren().add(wave);
            setLayoutX(getPosition().getX());
            setLayoutY(getPosition().getY());
            Color color = capture != null ? capture.getPlayer().getColor() : Color.WHITE;
            wave.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.25));
            wave.setStroke(color);
            wave.setStrokeWidth(2);
        }

        @Override
        protected void animate() {
            var next = wave.getRadius() + 0.75;
            if (next < ANIMATION_RANGE) {
                wave.setRadius(next);
                wave.setStrokeWidth(1 + next / ANIMATION_RANGE);
            } else {
                destroy();
            }
        }
    }
}
