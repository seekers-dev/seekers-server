package org.seekers.graphic;

import org.seekers.grpc.StatusReply;
import org.seekers.grpc.Switching;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Camp extends Rectangle implements Switching<StatusReply.Camp> {
	private final Game game;

	public Camp(Game game) {
		this.game = game;
		setFill(Color.TRANSPARENT);
		setStrokeWidth(game.getTypeProperties().getPropertieAsDouble("goal.radius"));
		setWidth(game.getTypeProperties().getPropertieAsDouble("camp.width"));
		setHeight(game.getTypeProperties().getPropertieAsDouble("camp.height"));
	}

	@Override
	public void switched(StatusReply.Camp delta) {
		setLayoutX(delta.getPosition().getX() - delta.getWidth() / 2);
		setLayoutY(delta.getPosition().getY() - delta.getHeight() / 2);
		setStroke(game.getHelper().getPlayers().get(delta.getPlayerId()).colorProperty().get());
	}
}
