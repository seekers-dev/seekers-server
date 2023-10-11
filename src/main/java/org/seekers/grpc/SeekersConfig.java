package org.seekers.grpc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeekerProperties class represents the properties for the Seeker game.
 */
public class SeekersConfig implements Corresponding<Map<String, String>> {

	private static final @Nonnull Logger logger = LoggerFactory.getLogger(SeekersConfig.class);

	private static @Nonnull SeekersConfig config = new SeekersConfig("server.ini");

	/**
	 * Sets the default SeekersConfig instance.
	 *
	 * @param config the default SeekersConfig instance to set.
	 */
	public static void setConfig(@Nonnull SeekersConfig config) {
		SeekersConfig.config = config;
	}

	/**
	 * Retrieves the default SeekersConfig instance.
	 *
	 * @return The default SeekersConfig instance.
	 */
	@Nonnull
	public static SeekersConfig getConfig() {
		return SeekersConfig.config;
	}

	@Nonnull
	private final Ini ini;

	/**
	 * Creates a SeekerProperties object and loads properties from the specified
	 * file.
	 *
	 * @param pathname The path of the properties file.
	 */
	public SeekersConfig(@Nonnull String pathname) {
		this.ini = new Ini();
		try {
			ini.load(new File(pathname));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * This method takes a key, a conversion function, and a default value. It
	 * checks if the key is already present in the used map and returns its value.
	 * If the key is not present, it checks if the properties contain the key and
	 * applies the conversion function to the corresponding value. If the key is not
	 * found in the properties, it returns the default value.
	 * 
	 * @param <T>     The type of the value to be returned.
	 * @param section The section of the <code>ini</code> file.
	 * @param option  The option of the section.
	 * @param func    The conversion function to convert the property value to the
	 *                desired type.
	 * @param value   The default value to be returned if the key is not found in the
	 *                properties.
	 * @return The value associated with the key, or the default value if the key is
	 *         not found.
	 */
	private <T> T getOrDefault(String section, String option, Function<String, T> func, T value) {
		String row = ini.fetch(section, option);
		return row == null ? value : func.apply(row);
	}

	public String getProjectExecCommand() {
		return getOrDefault("project", "exec-command", k -> k, "python3 run_clients.py");
	}

	public String getProjectPathToAis() {
		return getOrDefault("project", "path-to-ais", k -> k, "ais/");
	}

	private static final String GLOBAL = "global";

	/**
	 * Retrieves the value of the <code>"global.auto-play"</code> property.
	 *
	 * @return The value of the <code>"global.auto-play"</code> property.
	 */
	public boolean getGlobalAutoPlay() {
		return getOrDefault(GLOBAL, "auto-play", Boolean::valueOf, false);
	}

	/**
	 * Retrieves the value of the <code>"global.playtime"</code> property.
	 *
	 * @return The value of the <code>"global.playtime"</code> property.
	 */
	public long getGlobalPlaytime() {
		return getOrDefault(GLOBAL, "playtime", Long::valueOf, 50_000L);
	}

	/**
	 * Retrieves the value of the <code>"global.speed"</code> property.
	 *
	 * @return The value of the <code>"global.speed"</code> property.
	 */
	public double getGlobalSpeed() {
		return getOrDefault(GLOBAL, "speed", Double::valueOf, 1.0);
	}

	/**
	 * Retrieves the value of the <code>"global.players"</code> property.
	 *
	 * @return The value of the <code>"global.players"</code> property.
	 */
	public int getGlobalPlayers() {
		return getOrDefault(GLOBAL, "players", Integer::valueOf, 2);
	}

	/**
	 * Retrieves the value of the <code>"global.seekers"</code> property.
	 *
	 * @return The value of the <code>"global.seekers"</code> property.
	 */
	public int getGlobalSeekers() {
		return getOrDefault(GLOBAL, "seekers", Integer::valueOf, 5);
	}

	/**
	 * Retrieves the value of the <code>"global.goals"</code> property.
	 *
	 * @return The value of the <code>"global.goals"</code> property.
	 */
	public int getGlobalGoals() {
		return getOrDefault(GLOBAL, "goals", Integer::valueOf, 5);
	}

	/**
	 * Retrieves the value of the <code>"map.width"</code> property.
	 *
	 * @return The value of the <code>"map.width"</code> property.
	 */
	public int getMapWidth() {
		return getOrDefault("map", "width", Integer::valueOf, 768);
	}

	/**
	 * Retrieves the value of the <code>"map.height"</code> property.
	 *
	 * @return The value of the <code>"map.height"</code> property.
	 */
	public int getMapHeight() {
		return getOrDefault("map", "height", Integer::valueOf, 768);
	}

	/**
	 * Retrieves the value of the <code>"camp.width"</code> property.
	 *
	 * @return The value of the <code>"camp.width"</code> property.
	 */
	public double getCampWidth() {
		return getOrDefault("camp", "width", Double::valueOf, 55.0);
	}

	/**
	 * Retrieves the value of the <code>"camp.height"</code> property.
	 *
	 * @return The value of the <code>"camp.height"</code> property.
	 */
	public double getCampHeight() {
		return getOrDefault("camp", "height", Double::valueOf, 55.0);
	}

	/**
	 * Retrieves the value of the <code>"physical.friction"</code> property.
	 *
	 * @return The value of the <code>"physical.friction"</code> property.
	 */
	public double getPhysicalFriction() {
		return getOrDefault("physical", "friction", Double::valueOf, 0.1);
	}

	private static final String SEEKER = "seeker";

	/**
	 * Retrieves the value of the <code>"seeker.magnet-slowdown"</code> property.
	 *
	 * @return The value of the <code>"seeker.magnet-slowdown"</code> property.
	 */
	public double getSeekerMagnetSlowdown() {
		return getOrDefault(SEEKER, "magnet-slowdown", Double::valueOf, 0.2);
	}

	/**
	 * Retrieves the value of the <code>"seeker.disabled-time"</code> property.
	 *
	 * @return The value of the <code>"seeker.disabled-time"</code> property.
	 */
	public double getSeekerDisabledTime() {
		return getOrDefault(SEEKER, "disabled-time", Double::valueOf, 250.0);
	}

	/**
	 * Retrieves the value of the <code>"seeker.radius"</code> property.
	 *
	 * @return The value of the <code>"seeker.radius"</code> property.
	 */
	public double getSeekerRadius() {
		return getOrDefault(SEEKER, "radius", Double::valueOf, 10.0);
	}

	/**
	 * Retrieves the value of the <code>"seeker.mass"</code> property.
	 *
	 * @return The value of the <code>"seeker.mass"</code> property.
	 */
	public double getSeekerMass() {
		return getOrDefault(SEEKER, "mass", Double::valueOf, 1.0);
	}

	/**
	 * Retrieves the value of the <code>"seeker.thrust"</code> property.
	 *
	 * @return The value of the <code>"seeker.thrust"</code> property.
	 */
	public double getSeekerThrust() {
		return getOrDefault(SEEKER, "thrust", Double::valueOf, 0.1);
	}

	private static final String GOAL = "goal";

	/**
	 * Retrieves the value of the <code>"goal.scoring-time"</code> property.
	 *
	 * @return The value of the <code>"goal.scoring-time"</code> property.
	 */
	public double getGoalScoringTime() {
		return getOrDefault(GOAL, "scoring-time", Double::valueOf, 100.0);
	}

	/**
	 * Retrieves the value of the <code>"goal.radius"</code> property.
	 *
	 * @return The value of the <code>"goal.radius"</code> property.
	 */
	public double getGoalRadius() {
		return getOrDefault(GOAL, "radius", Double::valueOf, 6.0);
	}

	/**
	 * Retrieves the value of the <code>"goal.mass"</code> property.
	 *
	 * @return The value of the <code>"goal.mass"</code> property.
	 */
	public double getGoalMass() {
		return getOrDefault(GOAL, "mass", Double::valueOf, 0.5);
	}

	/**
	 * Retrieves the value of the <code>"goal.thrust"</code> property.
	 *
	 * @return The value of the <code>"goal.thrust"</code> property.
	 */
	public double getGoalThrust() {
		return getOrDefault(GOAL, "thrust", Double::valueOf, 0.1);
	}

	@Nonnull
	@Override
	public Map<String, String> associated() {
		Map<String, String> build = new HashMap<>();
		for (var section : ini.values()) {
			for (var entry : section.entrySet()) {
				build.put(section.getSimpleName() + "." + entry.getKey(), entry.getValue());
			}
		}
		return build;
	}
}
