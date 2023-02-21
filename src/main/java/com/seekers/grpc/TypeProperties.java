package com.seekers.grpc;

import java.util.Properties;

public class TypeProperties extends Properties {
	private static final long serialVersionUID = 6215399686445721594L;

	public String getPropertieAsString(String key) {
		return (String) get(key);
	}

	public Boolean getPropertieAsBoolean(String key) {
		Object val = get(key);
		if (val == null) {
			return null;
		}
		Boolean cast;
		if (val instanceof Boolean) {
			cast = (Boolean) val;
		} else {
			cast = Boolean.parseBoolean((String) val);
			put(key, cast);
		}
		return cast;
	}

	public Double getPropertieAsDouble(String key) {
		Object val = get(key);
		if (val == null) {
			return null;
		}
		Double cast;
		if (val instanceof Double) {
			cast = (Double) val;
		} else {
			cast = Double.valueOf((String) val);
			put(key, cast);
		}
		return cast;
	}

	public Integer getPropertieAsInteger(String key) {
		Object val = get(key);
		if (val == null) {
			return null;
		}
		Integer cast;
		if (val instanceof Integer) {
			cast = (Integer) val;
		} else {
			cast = Integer.valueOf((String) val);
			put(key, cast);
		}
		return cast;
	}
}
