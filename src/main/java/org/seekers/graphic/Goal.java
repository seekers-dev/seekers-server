package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.StatusReply;
import org.seekers.grpc.Switching;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Goal extends Circle implements Switching<StatusReply.Goal> {
	public Goal(App app) {
		super(app.getPropertieAsDouble("goal.radius"), Color.CORAL);
	}

	@Override
	public void switched(StatusReply.Goal delta) {
		setCenterX(delta.getSuper().getPosition().getX());
		setCenterY(delta.getSuper().getPosition().getY());
	}
}
