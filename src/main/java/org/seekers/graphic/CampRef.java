package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.CampStatus;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CampRef extends Rectangle implements Reference<CampStatus> {
	private App app;

	public CampRef(App app) {
		this.app = app;
		setFill(Color.TRANSPARENT);
		setStrokeWidth(app.getPropertieAsDouble("camp.stroke"));
		setWidth(app.getPropertieAsDouble("camp.width"));
		setHeight(app.getPropertieAsDouble("camp.height"));
	}

	@Override
	public void update(CampStatus delta) {
		setLayoutX(delta.getPosition().getX() - delta.getWidth() / 2);
		setLayoutY(delta.getPosition().getY() - delta.getHeight() / 2);
		setStroke(app.getPlayers().get(delta.getPlayerId()).color.get());
	}
}
