package com.seekers.graphic;

import io.scvis.proto.Reference;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Camp extends Rectangle implements Reference<com.seekers.grpc.game.Camp> {
	private final Game game;

	public Camp(Game game) {
		this.game = game;
		setFill(Color.TRANSPARENT);
		setStrokeWidth(game.getTypeProperties().getPropertieAsDouble("goal.radius"));
		setWidth(game.getTypeProperties().getPropertieAsDouble("camp.width"));
		setHeight(game.getTypeProperties().getPropertieAsDouble("camp.height"));
	}

	@Override
	public void update(com.seekers.grpc.game.Camp delta) {
		setLayoutX(delta.getPosition().getX() - delta.getWidth() / 2);
		setLayoutY(delta.getPosition().getY() - delta.getHeight() / 2);
		setStroke(game.getHelper().getPlayers().get(delta.getPlayerId()).colorProperty().get());
	}
}
