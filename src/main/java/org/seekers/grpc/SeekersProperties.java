package org.seekers.grpc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import io.scvis.proto.Corresponding;
import org.ini4j.Ini;

/**
 * The SeekerProperties class represents the properties for the Seeker game.
 */
public class SeekersProperties implements Corresponding<Map<String, String>> {
	@Nonnull
	private static SeekersProperties defaultProperties = new SeekersProperties("server.ini");

	/**
	 * Sets the default SeekerProperties instance.
	 *
	 * @param defaultProperties the default SeekerProperties instance to set.
	 */
	public static void setDefault(@Nonnull SeekersProperties defaultProperties) {
		SeekersProperties.defaultProperties = defaultProperties;
	}

	/**
	 * Retrieves the default SeekerProperties instance.
	 *
	 * @return The default SeekerProperties instance.
	 */
	@Nonnull
	public static SeekersProperties getDefault() {
		return SeekersProperties.defaultProperties;
	}

	@Nonnull
	private final Ini ini;

	/**
	 * Creates a SeekerProperties object and loads properties from the specified
	 * file.
	 *
	 * @param pathname The path of the properties file.
	 */
	public SeekersProperties(String pathname) {
		try {
			this.ini = new Ini(new File(pathname));
		}	catch (IOException e) {
			throw new SeekersException(e);
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

	/**
	 * Retrieves the value of the <code>"seeker.magnet-slowdown"</code> property.
	 *
	 * @return The value of the <code>"seeker.magnet-slowdown"</code> property.
	 */
	public double getSeekerMagnetSlowdown() {
		return getOrDefault("seeker", "magnet-slowdown", Double::valueOf, 0.2);
	}

	/**
	 * Retrieves the value of the <code>"seeker.disabled-time"</code> property.
	 *
	 * @return The value of the <code>"seeker.disabled-time"</code> property.
	 */
	public double getSeekerDisabledTime() {
		return getOrDefault("seeker", "disabled-time", Double::valueOf, 250.0);
	}

	/**
	 * Retrieves the value of the <code>"seeker.radius"</code> property.
	 *
	 * @return The value of the <code>"seeker.radius"</code> property.
	 */
	public double getSeekerRadius() {
		return getOrDefault("seeker", "radius", Double::valueOf, 10.0);
	}

	/**
	 * Retrieves the value of the <code>"seeker.mass"</code> property.
	 *
	 * @return The value of the <code>"seeker.mass"</code> property.
	 */
	public double getSeekerMass() {
		return getOrDefault("seeker", "mass", Double::valueOf, 1.0);
	}

	/**
	 * Retrieves the value of the <code>"seeker.thrust"</code> property.
	 *
	 * @return The value of the <code>"seeker.thrust"</code> property.
	 */
	public double getSeekerThrust() {
		return getOrDefault("seeker", "thrust", Double::valueOf, 0.1);
	}

	/**
	 * Retrieves the value of the <code>"goal.scoring-time"</code> property.
	 *
	 * @return The value of the <code>"goal.scoring-time"</code> property.
	 */
	public double getGoalScoringTime() {
		return getOrDefault("goal", "scoring-time", Double::valueOf, 100.0);
	}

	/**
	 * Retrieves the value of the <code>"goal.radius"</code> property.
	 *
	 * @return The value of the <code>"goal.radius"</code> property.
	 */
	public double getGoalRadius() {
		return getOrDefault("goal", "radius", Double::valueOf, 6.0);
	}

	/**
	 * Retrieves the value of the <code>"goal.mass"</code> property.
	 *
	 * @return The value of the <code>"goal.mass"</code> property.
	 */
	public double getGoalMass() {
		return getOrDefault("goal", "mass", Double::valueOf, 0.5);
	}

	/**
	 * Retrieves the value of the <code>"goal.thrust"</code> property.
	 *
	 * @return The value of the <code>"goal.thrust"</code> property.
	 */
	public double getGoalThrust() {
		return getOrDefault("goal", "thrust", Double::valueOf, 0.1);
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
		System.out.println(build);
		return build;
	}
}
