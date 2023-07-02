package org.seekers.grpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import javax.annotation.Nonnull;

import io.scvis.proto.Corresponding;

/**
 * The SeekerProperties class represents the properties for the Seeker game.
 */
public class SeekerProperties implements Corresponding<Map<String, String>> {
	@Nonnull
	private static SeekerProperties defaul = new SeekerProperties("server.properties");

	/**
	 * Sets the default SeekerProperties instance.
	 *
	 * @param defaul The default SeekerProperties instance to set.
	 */
	public static void setDefault(@Nonnull SeekerProperties defaul) {
		SeekerProperties.defaul = defaul;
	}

	/**
	 * Retrieves the default SeekerProperties instance.
	 *
	 * @return The default SeekerProperties instance.
	 */
	@Nonnull
	public static SeekerProperties getDefault() {
		return SeekerProperties.defaul;
	}

	@Nonnull
	private final Properties properties = new Properties();

	/**
	 * Creates a SeekerProperties object and loads properties from the specified
	 * file.
	 *
	 * @param pathname The path of the properties file.
	 */
	public SeekerProperties(String pathname) {
		try (FileInputStream file = new FileInputStream(new File(pathname))) {
			properties.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method takes a key, a conversion function, and a default value. It
	 * checks if the key is already present in the used map and returns its value.
	 * If the key is not present, it checks if the properties contain the key and
	 * applies the conversion function to the corresponding value. If the key is not
	 * found in the properties, it returns the default value.
	 * 
	 * @param <T>    The type of the value to be returned.
	 * @param key    The key whose associated value is to be retrieved.
	 * @param func   The conversion function to convert the property value to the
	 *               desired type.
	 * @param defaul The default value to be returned if the key is not found in the
	 *               properties.
	 * @return The value associated with the key, or the default value if the key is
	 *         not found.
	 */
	@SuppressWarnings("unchecked")
	private <T> T getOrDefault(String key, Function<String, T> func, T defaul) {
		return (T) used.computeIfAbsent(key,
				k -> properties.containsKey(key) ? func.apply(properties.getProperty(key)) : defaul);
	}

	@Nonnull
	private final Map<String, Object> used = new HashMap<>();

	public String getProjectPathToExec() {
		return getOrDefault("project.path-to-exec", k -> k, "run_clients.py");
	}

	public String getProjectPathToAis() {
		return getOrDefault("project.path-to-ais", k -> k, "ais/");
	}

	/**
	 * Retrieves the value of the "global.auto-play" property.
	 *
	 * @return The value of the "global.auto-play" property.
	 */
	public boolean getGlobalAutoPlay() {
		return getOrDefault("global.auto-play", Boolean::valueOf, false);
	}

	/**
	 * Retrieves the value of the "global.playtime" property.
	 *
	 * @return The value of the "global.playtime" property.
	 */
	public long getGlobalPlaytime() {
		return getOrDefault("global.playtime", Long::valueOf, 50_000l);
	}

	/**
	 * RetriThe getOrDefault() method is a generic private method thateves the value
	 * of the "global.speed" property.
	 *
	 * @return The value of the "global.speed" property.
	 */
	public double getGlobalSpeed() {
		return getOrDefault("global.speed", Double::valueOf, 1.0);
	}

	/**
	 * Retrieves the value of the "global.players" property.
	 *
	 * @return The value of the "global.players" property.
	 */
	public int getGlobalPlayers() {
		return getOrDefault("global.players", Integer::valueOf, 2);
	}

	/**
	 * Retrieves the value of the "global.seekers" property.
	 *
	 * @return The value of the "global.seekers" property.
	 */
	public int getGlobalSeekers() {
		return getOrDefault("global.seekers", Integer::valueOf, 5);
	}

	/**
	 * Retrieves the value of the "global.goals" property.
	 *
	 * @return The value of the "global.goals" property.
	 */
	public int getGlobalGoals() {
		return getOrDefault("global.goals", Integer::valueOf, 6);
	}

	/**
	 * Retrieves the value of the "map.width" property.
	 *
	 * @return The value of the "map.width" property.
	 */
	public int getMapWidth() {
		return getOrDefault("map.width", Integer::valueOf, 768);
	}

	/**
	 * Retrieves the value of the "map.height" property.
	 *
	 * @return The value of the "map.height" property.
	 */
	public int getMapHeight() {
		return getOrDefault("map.height", Integer::valueOf, 768);
	}

	/**
	 * Retrieves the value of the "camp.width" property.
	 *
	 * @return The value of the "camp.width" property.
	 */
	public int getCampWidth() {
		return getOrDefault("camp.width", Integer::valueOf, 55);
	}

	/**
	 * Retrieves the value of the "camp.height" property.
	 *
	 * @return The value of the "camp.height" property.
	 */
	public int getCampHeight() {
		return getOrDefault("camp.height", Integer::valueOf, 55);
	}

	/**
	 * Retrieves the value of the "physical.max-speed" property.
	 *
	 * @return The value of the "physical.max-speed" property.
	 */
	public double getPhysicalMaxSpeed() {
		return getOrDefault("physical.max-speed", Double::valueOf, 5.0);
	}

	/**
	 * Retrieves the value of the "physical.friction" property.
	 *
	 * @return The value of the "physical.friction" property.
	 */
	public double getPhysicalFriction() {
		return getOrDefault("physical.friction", Double::valueOf, 0.02);
	}

	/**
	 * Retrieves the value of the "seeker.magnet-slowdown" property.
	 *
	 * @return The value of the "seeker.magnet-slowdown" property.
	 */
	public double getSeekerMagnetSlowdown() {
		return getOrDefault("seeker.magnet-slowdown", Double::valueOf, 0.2);
	}

	/**
	 * Retrieves the value of the "seeker.disabled-time" property.
	 *
	 * @return The value of the "seeker.disabled-time" property.
	 */
	public double getSeekerDisabledTime() {
		return getOrDefault("seeker.disbaled-time", Double::valueOf, 75.0);
	}

	/**
	 * Retrieves the value of the "seeker.radius" property.
	 *
	 * @return The value of the "seeker.radius" property.
	 */
	public double getSeekerRadius() {
		return getOrDefault("seeker.radius", Double::valueOf, 10.0);
	}

	/**
	 * Retrieves the value of the "seeker.mass" property.
	 *
	 * @return The value of the "seeker.mass" property.
	 */
	public double getSeekerMass() {
		return getOrDefault("seeker.mass", Double::valueOf, 1.0);
	}

	/**
	 * Retrieves the value of the "goal.scoring-time" property.
	 *
	 * @return The value of the "goal.scoring-time" property.
	 */
	public double getGoalScoringTime() {
		return getOrDefault("goal.scoring-time", Double::valueOf, 100.0);
	}

	/**
	 * Retrieves the value of the "goal.radius" property.
	 *
	 * @return The value of the "goal.radius" property.
	 */
	public double getGoalRadius() {
		return getOrDefault("goal.radius", Double::valueOf, 6.0);
	}

	/**
	 * Retrieves the value of the "goal.mass" property.
	 *
	 * @return The value of the "goal.mass" property.
	 */
	public double getGoalMass() {
		return getOrDefault("goal.mass", Double::valueOf, 0.5);
	}

	@Nonnull
	@Override
	public Map<String, String> associated() {
		Map<String, String> build = new HashMap<>();
		for (Map.Entry<String, Object> entry : used.entrySet()) {
			build.put(entry.getKey(), entry.getValue().toString());
		}
		return build;
	}
}
