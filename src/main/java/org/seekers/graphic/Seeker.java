package org.seekers.graphic;

import java.util.ArrayList;
import java.util.List;

import org.seekers.grpc.StatusReply;
import org.seekers.grpc.Switching;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Seeker extends Pane implements Switching<StatusReply.Seeker> {
	private Game game;

	private final List<ScaleTransition> transitions = new ArrayList<>();

	private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>();

	private final Circle circle;

	public Seeker(Game game) {
		this.game = game;

		double radius = game.getTypeProperties().getPropertieAsDouble("seeker.radius");
		circle = new Circle(radius);
		circle.fillProperty().bind(fill);

		for (int i = 0; i < 3; i++) {
			Circle magnet = new Circle(radius, Color.TRANSPARENT);
			magnet.strokeProperty().bind(fill);
			magnet.strokeWidthProperty().bind(new SimpleDoubleProperty(2).subtract(magnet.scaleXProperty().divide(1.5)));
			getChildren().add(magnet);

			ScaleTransition transition = new ScaleTransition(Duration.millis(450), magnet);
			transition.setByX(1);
			transition.setByY(1);
			transition.setToX(2);
			transition.setToY(2);
			transition.setDelay(Duration.millis(150 * i));
			transition.setCycleCount((int) Double.POSITIVE_INFINITY);
			transition.play();

			transitions.add(transition);
		}

		getChildren().add(circle);
	}

	@Override
	public void switched(StatusReply.Seeker delta) {
		setLayoutX(delta.getSuper().getPosition().getX() - 5);
		setLayoutY(delta.getSuper().getPosition().getY() - 5);

		Color natural = game.getHelper().getPlayers().get(delta.getPlayerId()).colorProperty().get();
		Paint paint = delta.getDisableCounter() > 0 ? natural.darker().darker() : natural;
		fill.set(paint);
		if (delta.getDisableCounter() > 0 || delta.getMagnet() == 0) {
			transitions.forEach(e -> e.getNode().setVisible(false));
		} else if (delta.getMagnet() > 0) {
			transitions.forEach(e -> {
				e.getNode().setVisible(true);
				e.setByX(1);
				e.setByY(1);
				e.setToX(2);
				e.setToY(2);
			});
		} else {
			transitions.forEach(e -> {
				e.getNode().setVisible(true);
				e.setByX(2);
				e.setByY(2);
				e.setToX(1);
				e.setToY(1);
			});
		}
	}
}
