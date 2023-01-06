package org.seekers.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.seekers.grpc.PushHelper;

import javafx.geometry.Point2D;

public class Game {
	static final Properties DEFAULT = new Properties();
	static {
		DEFAULT.putAll(Map.ofEntries(Map.entry("global.auto-play", "true"), Map.entry("global.playtime", "5000"),
				Map.entry("global.speed", "1"), Map.entry("global.players", "2"), Map.entry("global.seekers", "5"),
				Map.entry("global.goals", "6"), Map.entry("map.width", "768"), Map.entry("map.height", "768"),
				Map.entry("camp.width", "45"), Map.entry("camp.height", "45"), Map.entry("physical.max-speed", "5"),
				Map.entry("physical.friction", ".02"), Map.entry("seeker.magnet-slowdown", ".2"),
				Map.entry("seeker.disabled-time", "25"), Map.entry("seeker.radius", "10"),
				Map.entry("seeker.mass", "1"), Map.entry("goal.scoring-time", "100"), Map.entry("goal.radius", "6"),
				Map.entry("goal.mass", ".5")));
	}

	private final Map<String, PushHelper> helpers = new HashMap<>();

	private final Set<Physical> physicals = new HashSet<>();
	private final Set<Seeker> seekers = new HashSet<>();
	private final Set<Player> players = new HashSet<>();
	private final Set<Goal> goals = new HashSet<>();
	private final Set<Camp> camps = new HashSet<>();

	private final Properties properties = new Properties();

	private double width, height, speed, passed, playtime;
	private int playerCount, seekerCount, goalCount;
	private boolean autoPlay;

	private final Clock clock = new Clock(new Runnable() {
		public void run() {
			if(hasOpenSlots()) return;
			double before = passed;
			passed = Math.min(passed + speed, playtime);
			for (Entity entity : physicals) {
				entity.update(passed - before);
				if (autoPlay && entity instanceof Seeker) {
					((Seeker) entity).setAutoCommands();
				}
			}
		}
	}, 5l);

	public Game(File file) {
		if (file.exists() && file.getName().endsWith(".properties"))
			try (FileInputStream stream = new FileInputStream(file)) {
				properties.load(stream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			properties.putAll(DEFAULT);
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
				addPlayer();
		}
		addGoals();
	}

	public boolean hasOpenSlots() {
		return players.size() < playerCount;
	}

	public Player addPlayer() {
		int cur = players.size(), max = playerCount;

		Player player = new Player(this);
		player.setCamp(new Camp(player, new Point2D(width * 0.5, height * (max - cur) / (max + 1))));
		for (int s = 0; s < seekerCount; s++) {
			new Seeker(player, getRandomPosition());
		}

		return player;
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

	public Map<String, PushHelper> getHelpers() {
		return helpers;
	}

	public Set<Physical> getPhysicals() {
		return physicals;
	}

	public Set<Seeker> getSeekers() {
		return seekers;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public Set<Goal> getGoals() {
		return goals;
	}

	public Set<Camp> getCamps() {
		return camps;
	}

	public Properties getProperties() {
		return properties;
	}

	public Clock getClock() {
		return clock;
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

	public double getMaxPlaytime() {
		return playtime;
	}

	public double getPassedPlaytime() {
		return passed;
	}
}
