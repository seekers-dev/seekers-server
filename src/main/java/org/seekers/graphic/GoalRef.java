package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.GoalStatus;

import javafx.scene.shape.Circle;

public class GoalRef extends Circle implements Reference<GoalStatus> {
	public GoalRef(App app) {
		super(app.getPropertieAsDouble("goal.radius"));
	}

	@Override
	public void update(GoalStatus delta) {
		setCenterX(delta.getSuper().getPosition().getX());
		setCenterY(delta.getSuper().getPosition().getY());
	}
}
