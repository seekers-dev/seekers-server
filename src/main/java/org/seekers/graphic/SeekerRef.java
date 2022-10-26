package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.SeekerStatus;

import javafx.animation.ScaleTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class SeekerRef extends StackPane implements Reference<SeekerStatus> {
	private App app;

	private final Circle circle, magnet;
	private final ScaleTransition transition;

	public SeekerRef(App app) {
		this.app = app;

		double radius = app.getPropertieAsDouble("seeker.radius");
		circle = new Circle(radius);
		magnet = new Circle(radius, Color.TRANSPARENT);
		magnet.setStrokeWidth(1);
		getChildren().addAll(circle, magnet);

		transition = new ScaleTransition(Duration.millis(400), magnet);
		transition.setByX(1.1);
		transition.setByY(1.1);
		transition.setToX(1.6);
		transition.setToY(1.6);
		transition.setCycleCount((int) Double.POSITIVE_INFINITY);
		transition.setAutoReverse(true);
		transition.play();
	}

	@Override
	public void update(SeekerStatus delta) {
		setLayoutX(delta.getSuper().getPosition().getX() - 5);
		setLayoutY(delta.getSuper().getPosition().getY() - 5);

		Color natural = app.getPlayers().get(delta.getPlayerId()).colorProperty().get();
		Paint paint = delta.getDisableCounter() > 0 ? natural.darker().darker() : natural;
		circle.setFill(paint);
		magnet.setStroke(paint);
		if (delta.getDisableCounter() > 0) {
			magnet.setVisible(false);
		} else {
			magnet.setVisible(true);
		}
	}
}
