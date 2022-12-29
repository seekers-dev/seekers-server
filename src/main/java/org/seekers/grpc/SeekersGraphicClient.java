package org.seekers.grpc;

import org.seekers.graphic.Game;

import javafx.scene.layout.BorderPane;

public class SeekersGraphicClient extends SeekersClient {
	private final Game game = new Game(new BorderPane());

	public SeekersGraphicClient() {
		game.start(this);
	}

	public Game getGame() {
		return game;
	}
}
