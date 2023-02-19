package org.seekers.graphic;

import java.util.Collection;

import org.seekers.grpc.GetHelper;
import org.seekers.grpc.PropertiesReply;
import org.seekers.grpc.SeekersClient;
import org.seekers.grpc.TypeProperties;

import javafx.animation.AnimationTimer;
import javafx.collections.MapChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Game extends Scene {
	private final TypeProperties properties = new TypeProperties();

	private final GetHelper helper = new GetHelper(this);

	public Game(BorderPane root) {
		super(root, 768, 768, true, SceneAntialiasing.BALANCED);

		Group group = new Group();
		VBox box = new VBox(-5);

		helper.getPlayers().addListener(getListener(box.getChildren()));
		helper.getSeekers().addListener(getListener(group.getChildren()));
		helper.getGoals().addListener(getListener(group.getChildren()));
		helper.getCamps().addListener(getListener(group.getChildren()));

		root.setBackground(new Background(new BackgroundFill(Color.gray(.1), null, null)));
		root.getChildren().add(group);
		root.setTop(box);
	}

	public void start(SeekersClient client) {
		client.join("", "");

		PropertiesReply propertiesReply = client.getProperties();
		properties.putAll(propertiesReply.getEntriesMap());

		AnimationTimer timer = new AnimationTimer() {
			final long[] frameTimes = new long[100];
			int frameTimeIndex = 0;
			boolean arrayFilled = false;

			@Override
			public void handle(long now) {
				frameTimes[frameTimeIndex] = now;
				frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
				if (frameTimeIndex == 0) {
					arrayFilled = true;
				}
				if (arrayFilled) {
					helper.update(client.getStatus());
				}
			}
		};
		timer.start();
	}

	private MapChangeListener<String, Node> getListener(Collection<Node> to) {
		return new MapChangeListener<>() {
			@Override
			public void onChanged(Change<? extends String, ? extends Node> change) {
				if (change.wasAdded())
					to.add(change.getValueAdded());
				if (change.wasRemoved())
					to.remove(change.getValueRemoved());
			}
		};
	}

	public TypeProperties getTypeProperties() {
		return properties;
	}

	public GetHelper getHelper() {
		return helper;
	}

}
