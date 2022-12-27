package org.seekers.graphic;

import org.seekers.App;
import org.seekers.grpc.PlayerStatus;
import org.seekers.grpc.Switching;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Player extends Label implements Switching<PlayerStatus> {
	private final ObjectProperty<Color> color = new SimpleObjectProperty<>();
	private final IntegerProperty score = new SimpleIntegerProperty();

	public Player(App app) {
		textProperty().bind(new SimpleStringProperty("Score: ").concat(score));
		textFillProperty().bind(color);
		setFont(Font.font("ubuntu", 20));
	}

	@Override
	public void switched(PlayerStatus delta) {
		color.set(Color.web(delta.getColor()));
		score.set(delta.getScore());
	}

	public ObjectProperty<Color> colorProperty() {
		return color;
	}

	public IntegerProperty scoreProperty() {
		return score;
	}
}
