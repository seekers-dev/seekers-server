package com.seekers.graphic;

import io.scvis.proto.Reference;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Goal extends Circle implements Reference<com.seekers.grpc.game.Goal> {
	public Goal(Game game) {
		super(game.getTypeProperties().getPropertieAsDouble("goal.radius"), Color.CORAL);
	}

	@Override
	public void update(com.seekers.grpc.game.Goal delta) {
		setCenterX(delta.getSuper().getPosition().getX());
		setCenterY(delta.getSuper().getPosition().getY());
	}
}
