package org.seekers.graphic;

import org.seekers.grpc.StatusReply;

import com.karlz.exchange.Reference;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Camp extends Rectangle implements Reference<StatusReply.Camp> {
	private final Game game;

	public Camp(Game game) {
		this.game = game;
		setFill(Color.TRANSPARENT);
		setStrokeWidth(game.getTypeProperties().getPropertieAsDouble("goal.radius"));
		setWidth(game.getTypeProperties().getPropertieAsDouble("camp.width"));
		setHeight(game.getTypeProperties().getPropertieAsDouble("camp.height"));
	}

	@Override
	public void update(StatusReply.Camp delta) {
		setLayoutX(delta.getPosition().getX() - delta.getWidth() / 2);
		setLayoutY(delta.getPosition().getY() - delta.getHeight() / 2);
		setStroke(game.getHelper().getPlayers().get(delta.getPlayerId()).colorProperty().get());
	}
}
