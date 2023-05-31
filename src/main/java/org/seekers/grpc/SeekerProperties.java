package org.seekers.grpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import io.scvis.proto.Corresponding;

public class SeekerProperties implements Corresponding<Map<String, String>> {

	private static SeekerProperties defaul = new SeekerProperties("server.properties");

	public static void setDefault(SeekerProperties defaul) {
		SeekerProperties.defaul = defaul;
	}

	public static SeekerProperties getDefault() {
		return SeekerProperties.defaul;
	}

	private Properties properties = new Properties();

	public SeekerProperties(String pathname) {
		try {
			properties.load(new FileInputStream(new File(pathname)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getOrDefault(String key, Function<String, T> func, T defaul) {
		return (T) used.computeIfAbsent(key, (k) -> {
			return properties.containsKey(key) ? func.apply(properties.getProperty(key)) : defaul;
		});
	}

	private Map<String, Object> used = new HashMap<>();

	public boolean getGlobalAutoPlay() {
		return getOrDefault("global.auto-play", Boolean::valueOf, false);
	}

	public long getGlobalPlaytime() {
		return getOrDefault("global.playtime", Long::valueOf, 50_000l);
	}

	public double getGlobalSpeed() {
		return getOrDefault("global.speed", Double::valueOf, 1.0);
	}

	public int getGlobalPlayers() {
		return getOrDefault("global.players", Integer::valueOf, 2);
	}

	public int getGlobalSeekers() {
		return getOrDefault("global.seekers", Integer::valueOf, 5);
	}

	public int getGlobalGoals() {
		return getOrDefault("global.goals", Integer::valueOf, 6);
	}

	public int getMapWidth() {
		return getOrDefault("map.width", Integer::valueOf, 768);
	}

	public int getMapHeight() {
		return getOrDefault("map.height", Integer::valueOf, 768);
	}

	public int getCampWidth() {
		return getOrDefault("camp.width", Integer::valueOf, 55);
	}

	public int getCampHeight() {
		return getOrDefault("camp.height", Integer::valueOf, 55);
	}

	public double getPhysicalMaxSpeed() {
		return getOrDefault("physical.max-speed", Double::valueOf, 5.0);
	}

	public double getPhysicalFriction() {
		return getOrDefault("physical.friction", Double::valueOf, 0.02);
	}

	public double getSeekerMagnetSlowdown() {
		return getOrDefault("seeker.magnet-slowdown", Double::valueOf, 0.2);
	}

	public double getSeekerDisabledTime() {
		return getOrDefault("seeker.disbaled-time", Double::valueOf, 75.0);
	}

	public double getSeekerRadius() {
		return getOrDefault("seeker.radius", Double::valueOf, 10.0);
	}

	public double getSeekerMass() {
		return getOrDefault("seeker.mass", Double::valueOf, 1.0);
	}

	public double getGoalScoringTime() {
		return getOrDefault("goal.scoring-time", Double::valueOf, 100.0);
	}

	public double getGoalRadius() {
		return getOrDefault("goal.radius", Double::valueOf, 6.0);
	}

	public double getGoalMass() {
		return getOrDefault("goal.mass", Double::valueOf, 0.5);
	}

	@Override
	public Map<String, String> associated() {
		Map<String, String> build = new HashMap<>();
		for (Map.Entry<String, Object> entry : used.entrySet()) {
			build.put(entry.getKey(), entry.getValue().toString());
		}
		return build;
	}
}
