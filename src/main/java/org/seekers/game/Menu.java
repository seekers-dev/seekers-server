package org.seekers.game;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Menu extends Pane {
	private class Continue extends Pane {
		public Continue() {
			getChildren().add(new Label("Continue", new Circle(5, Color.CADETBLUE)));
			setOnMouseClicked(e -> Menu.this.setVisible(false));
		}
	}

	private class Leaderboard extends Pane {
		public Leaderboard() {
			GridPane pane = new GridPane() {

			};
			pane.addRow(0, new Label("[0]"), new Label("[1]"), new Label("[2]"));
			getChildren().add(pane);
		}
	}

	private class Exit extends Pane {
		public Exit() {
			getChildren().add(new Label("Exit", new Circle(5, Color.RED)));
			setOnMouseClicked(e -> {
				getScene().getWindow().getOnCloseRequest().handle(null);
				getScene().getWindow().hide();
			});
		}
	}

	private final VBox box = new VBox();
	{
		box.getChildren().addAll(new Continue(), new Leaderboard(), new Exit());
		box.setPadding(new Insets(20));
	}

	private double startX, startY;
	private boolean dragged = false;

	public Menu() {
		Rectangle back = new Rectangle();
		back.widthProperty().bind(box.widthProperty());
		back.heightProperty().bind(box.heightProperty());
		back.setFill(Color.WHITE);
		back.setArcHeight(45);
		back.setArcWidth(45);
		getChildren().add(back);
		getChildren().add(box);

		addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				if (dragged) {
					int newX = (int) capp(getLayoutX() - startX + e.getSceneX(), 0, 680);
					int newY = (int) capp(getLayoutY() - startY + e.getSceneY(), 0, 680);
					setLayoutX(newX);
					setLayoutY(newY);
					startX = e.getSceneX();
					startY = e.getSceneY();
				}
				dragged = true;
				setCursor(Cursor.MOVE);
				startX = e.getSceneX();
				startY = e.getSceneY();
			}
		});
		addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			dragged = false;
			setCursor(Cursor.DEFAULT);
		});
	}

	private static double capp(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}
}
