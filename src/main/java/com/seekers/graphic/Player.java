package com.seekers.graphic;

import com.karlz.exchange.Reference;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Player extends Label implements Reference<com.seekers.grpc.game.Player> {
	private final ObjectProperty<Color> color = new SimpleObjectProperty<>();
	private final StringProperty name = new SimpleStringProperty();
	private final IntegerProperty score = new SimpleIntegerProperty();

	public Player(Game game) {
		textProperty().bind(name.concat(": ").concat(score));
		textFillProperty().bind(color);
		setFont(Font.font("ubuntu", 20));
		setPadding(new Insets(5));
	}

	@Override
	public void update(com.seekers.grpc.game.Player delta) {
		color.set(Color.web(delta.getColor()));
		name.set(delta.getName());
		score.set(delta.getScore());
	}

	public ObjectProperty<Color> colorProperty() {
		return color;
	}

	public IntegerProperty scoreProperty() {
		return score;
	}
}
