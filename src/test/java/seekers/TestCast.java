package seekers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class TestCast {

	Properties properties = new Properties();

	TestCast() {
		properties.putAll(Map.of("width", "768.0", "height", "768.0", "playtime", "5_000.0", "tournement", "true",
				"global.players", "2", "seekers.size", "10"));
	}

	@Test
	void content() {
		assertEquals(768.0, getPropertieAsDouble("width"));

		assertEquals(true, getPropertieAsBoolean("tournement"));

		assertEquals(2, getPropertieAsInteger("global.players"));

		assertEquals(10.0, getPropertieAsDouble("seekers.size"));
	}

	String getPropertieAsString(String key) {
		return (String) properties.get(key);
	}

	Boolean getPropertieAsBoolean(String key) {
		Object val = properties.get(key);

		if (val == null) {
			return null;
		}
		Boolean cast;
		if (val instanceof Boolean) {
			cast = (Boolean) val;
			properties.put(key, cast);
		} else {
			cast = Boolean.parseBoolean((String) val);
		}
		return cast;
	}

	Double getPropertieAsDouble(String key) {
		Object val = properties.get(key);

		if (val == null) {
			return null;
		}
		Double cast;
		if (val instanceof Double) {
			cast = (Double) val;
			properties.put(key, cast);
		} else {
			cast = Double.valueOf((String) val);
		}
		return cast;
	}

	Integer getPropertieAsInteger(String key) {
		Object val = properties.get(key);

		if (val == null) {
			return null;
		}
		Integer cast;
		if (val instanceof Integer) {
			cast = (Integer) val;
			properties.put(key, cast);
		} else {
			cast = Integer.valueOf((String) val);
		}
		return cast;
	}
}
