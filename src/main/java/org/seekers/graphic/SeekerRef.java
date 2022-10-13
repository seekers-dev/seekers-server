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

	Circle circle = new Circle(10);
	Circle magnet = new Circle(10, Color.TRANSPARENT);

	ScaleTransition transition = new ScaleTransition(Duration.millis(400), magnet);

	public SeekerRef(App app) {
		this.app = app;
		double radius = app.getPropertieAsDouble("seeker.radius");
		circle = new Circle(radius);
		magnet = new Circle(radius, Color.TRANSPARENT);
		getChildren().addAll(circle, magnet);
		magnet.setStrokeWidth(1);
		transition.setByX(1.0);
		transition.setByY(1.0);
		transition.setToX(1.5);
		transition.setToY(1.5);
		transition.setCycleCount((int) Double.POSITIVE_INFINITY);
		transition.setAutoReverse(true);
		transition.play();
	}

	@Override
	public void update(SeekerStatus delta) {
		setLayoutX(delta.getSuper().getPosition().getX() - 5);
		setLayoutY(delta.getSuper().getPosition().getY() - 5);
		Paint paint = delta.getDisableCounter() > 0 ? Color.BLACK
				: app.getPlayers().get(delta.getPlayerId()).color.get();
		circle.setFill(paint);
		magnet.setStroke(paint);
		if (delta.getDisableCounter() > 0) {
			magnet.setVisible(false);
		} else {
			magnet.setVisible(true);
		}
	}
}
