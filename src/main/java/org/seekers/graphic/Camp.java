package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.StatusReply;
import org.seekers.grpc.Switching;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Camp extends Rectangle implements Switching<StatusReply.Camp> {
	private App app;

	public Camp(App app) {
		this.app = app;
		setFill(Color.TRANSPARENT);
		setStrokeWidth(app.getPropertieAsDouble("goal.radius"));
		setWidth(app.getPropertieAsDouble("camp.width"));
		setHeight(app.getPropertieAsDouble("camp.height"));
	}

	@Override
	public void switched(StatusReply.Camp delta) {
		setLayoutX(delta.getPosition().getX() - delta.getWidth() / 2);
		setLayoutY(delta.getPosition().getY() - delta.getHeight() / 2);
		setStroke(app.getPlayers().get(delta.getPlayerId()).colorProperty().get());
	}
}
