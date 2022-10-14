package org.seekers.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javafx.geometry.Point2D;

public class World {
	static final File DEFAULT = new File("src/main/resources/default.properties");

	private final Map<String, Physical> physicals = new HashMap<>();
	private final Map<String, Seeker> seekers = new HashMap<>();
	private final Map<String, Player> players = new HashMap<>();
	private final Map<String, Goal> goals = new HashMap<>();
	private final Map<String, Camp> camps = new HashMap<>();

	private final Properties properties = new Properties();

	transient double passed = 0;

	private final Entity updater = new Entity() {
		@Override
		public void update(double deltaT) {
			double before = passed;
			passed = Math.min(passed + deltaT * speed, playtime);
			for (Entity entity : physicals.values()) {
				entity.update(passed - before);
				if (autoPlay && entity instanceof Seeker) {
					((Seeker) entity).setAutoCommands();
				}
			}
		}
	};;

	private final double width, height, speed, playtime;
	private final int playerCount, seekerCount, goalCount;
	private final boolean autoPlay;

	public World() {
		this(DEFAULT);
	}

	public World(File file) {
		try (FileInputStream stream = new FileInputStream(
				file.exists() && file.getName().endsWith(".properties") ? file : DEFAULT)) {
			properties.load(stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.width = Double.valueOf(properties.getProperty("map.width"));
		this.height = Double.valueOf(properties.getProperty("map.height"));
		this.speed = Double.valueOf(properties.getProperty("global.speed"));
		this.playtime = Double.valueOf(properties.getProperty("global.playtime"));
		this.playerCount = Integer.valueOf(properties.getProperty("global.players"));
		this.seekerCount = Integer.valueOf(properties.getProperty("global.seekers"));
		this.goalCount = Integer.valueOf(properties.getProperty("global.goals"));
		this.autoPlay = Boolean.parseBoolean(properties.getProperty("global.auto-play"));
		if (autoPlay) {
			while (hasOpenSlots())
				addPlayer(new String());
		}
		addGoals();
	}

	public boolean hasOpenSlots() {
		return players.size() < playerCount;
	}

	public String addPlayer(String token) {
		int cur = players.size(), max = playerCount;

		Player player = new Player(this, token);
		player.setCamp(new Camp(player, new Point2D(width * 0.5, height * (max - cur) / (max + 1))));
		for (int s = 0; s < seekerCount; s++) {
			new Seeker(player, getRandomPosition());
		}

		return player.toString();
	}

	public void addGoals() {
		for (int i = 0; i < goalCount; i++) {
			new Goal(this, getRandomPosition());
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

	public Properties getProperties() {
		return properties;
	}

	public Entity getUpdater() {
		return updater;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getMaxPlaytime() {
		return playtime;
	}

	public double getPassedPlaytime() {
		return passed;
	}
}
