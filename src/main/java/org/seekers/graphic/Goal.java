package org.seekers.graphic;

import org.seekers.grpc.StatusReply;

import com.karlz.exchange.Reference;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Goal extends Circle implements Reference<StatusReply.Goal> {
	public Goal(Game game) {
		super(game.getTypeProperties().getPropertieAsDouble("goal.radius"), Color.CORAL);
	}

	@Override
	public void update(StatusReply.Goal delta) {
		setCenterX(delta.getSuper().getPosition().getX());
		setCenterY(delta.getSuper().getPosition().getY());
	}
}
