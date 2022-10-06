package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.PlayerStatus;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class PlayerRef extends Label implements Reference<PlayerStatus> {
	ObjectProperty<Paint> color = new SimpleObjectProperty<>();
	IntegerProperty score = new SimpleIntegerProperty();

	public PlayerRef(App app) {
		textProperty().bind(new SimpleStringProperty("Score: ").concat(score));
		textFillProperty().bind(color);
		setFont(Font.font("ubuntu", 20));
	}

	@Override
	public void update(PlayerStatus delta) {
		color.set(Color.web(delta.getColor()));
		score.set(delta.getScore());
	}
}
