package org.seekers.grpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;

public interface Buildable {
	public Object asBuilder();

	public default Vector asVector(Point2D p) {
		return Vector.newBuilder().setX(p.getX()).setY(p.getY()).build();
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> map(Map<K, Buildable> src) {
		Map<K, V> map = new HashMap<>(src.size());
		for (Map.Entry<K, Buildable> entry : src.entrySet()) {
			map.put(entry.getKey(), (V) entry.getValue().asBuilder());
		}
		return map;
	}

	@Deprecated(since = "0.0.2-SNAPSHOT")
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> collect(Collection<Buildable> src) {
		List<T> list = new ArrayList<>(src.size());
		for (Buildable entry : src) {
			list.add((T) entry.asBuilder());
		}
		return list;
	}
}
