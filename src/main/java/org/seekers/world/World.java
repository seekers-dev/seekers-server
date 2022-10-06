package org.seekers.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javafx.geometry.Point2D;

public class World {
	static Properties DEFAULT = new Properties();
	static {
		DEFAULT.putAll(Map.of("width", 728, "height", 728, "playtime", 10_000));
	}

	private Properties properties = new Properties(DEFAULT);

	private final Map<String, Physical> physicals = new HashMap<>();
	private final Map<String, Seeker> seekers = new HashMap<>();
	private final Map<String, Player> players = new HashMap<>();
	private final Map<String, Goal> goals = new HashMap<>();
	private final Map<String, Camp> camps = new HashMap<>();

	private double playtime;

	public boolean isRunning() {
		return playtime > 0;
	}

	private final Entity updater = new Entity() {
		@Override
		public void update(double deltaT) {
			if (World.this.isRunning()) {
				for (Entity entity : physicals.values()) {
					entity.update(deltaT);
				}
				playtime -= deltaT;
			} else {
				List<Player> players = new ArrayList<>(World.this.players.values());
				players.sort((a, b) -> a.getScore() - b.getScore());
				players.forEach(c -> System.out.println(new String(c.getToken()) + " " + c.getScore()));
			}
		}
	};

	public Entity getUpdater() {
		return updater;
	}

	private final double width;
	private final double height;

	public World(double width, double height, double playtime) {
		this.width = width;
		this.height = height;
		this.playtime = playtime;
		start();
	}

	public World(InputStream stream) {
		try {
			this.properties.load(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.width = (double) properties.get("width");
		this.height = (double) properties.get("height");
		this.playtime = (double) properties.get("playtime");
	}

	public void start() {
		for (int i = 0; i < 6; i++) {
			new Goal(this, getRandomPosition());
		}
		for (int i = 0; i < 2; i++) {
			Player player = new Player(this, "signum");
			player.setCamp(new Camp(player, new Point2D(width * 0.5, height * (2 - i) / 3)));
			for (int j = 0; j < 5; j++) {
				new Seeker(player, getRandomPosition());
			}
		}
	}

	public void putNormalizedPosition(Physical physical) {
		Point2D p = physical.getPosition();

		physical.setPosition(physical.getPosition().subtract(Math.floor(p.getX() / width) * width,
				Math.floor(p.getY() / height) * height));
	}

	public Physical getNearestPhysicalOf(Point2D p, Collection<Physical> physicals) {
		if (physicals.isEmpty())
			return null;

		double distance = width * height;
		Physical nearest = null;

		for (Physical physical : physicals) {
			double dif = getTorusDistance(p, physical.getPosition());
			if (dif < distance) {
				distance = dif;
				nearest = physical;
			}
		}
		return nearest;
	}

	private double distance(double p0, double p1, double d) {
		double temp = Math.abs(p0 - p1);
		return Math.min(temp, d - temp);
	}

	public double getTorusDistance(Point2D p0, Point2D p1) {
		return new Point2D(distance(p0.getX(), p1.getX(), width), distance(p0.getY(), p1.getY(), height)).magnitude();
	}

	private double difference(double p0, double p1, double d) {
		double temp = Math.abs(p0 - p1);
		return (temp < d - temp) ? p1 - p0 : p0 - p1;
	}

	public Point2D getTorusDifference(Point2D p0, Point2D p1) {
		return new Point2D(difference(p0.getX(), p1.getX(), width), difference(p0.getY(), p1.getY(), height));
	}

	public Point2D getTorusDirection(Point2D p0, Point2D p1) {
		return getTorusDifference(p0, p1).normalize();
	}

	public Map<String, Physical> getPhysicals() {
		return physicals;
	}

	public Map<String, Seeker> getSeekers() {
		return seekers;
	}

	public Map<String, Player> getPlayers() {
		return players;
	}

	public Map<String, Goal> getGoals() {
		return goals;
	}

	public Map<String, Camp> getCamps() {
		return camps;
	}

	public Point2D getRandomPosition() {
		return new Point2D(Math.random() * width, Math.random() * height);
	}

	public double getDiameter() {
		return Math.hypot(width, height);
	}

	public Point2D getCenter() {
		return new Point2D(width / 2, height / 2);
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
}
