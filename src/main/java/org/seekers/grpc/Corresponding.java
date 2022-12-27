package org.seekers.grpc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javafx.geometry.Point2D;

public interface Corresponding<T> {
	T associated();

	public static <T> Collection<T> transform(Collection<? extends Corresponding<T>> collection) {
		java.util.Vector<T> transformed = new java.util.Vector<>(collection.size());
		int index = 0;
		for (Iterator<? extends Corresponding<T>> iterator = collection.iterator(); iterator.hasNext(); index++) {
			transformed.add(index, iterator.next().associated());
		}
		return transformed;
	}

	public static <K, V> Map<K, V> transform(Map<K, Corresponding<V>> src) {
		Map<K, V> map = new HashMap<>(src.size());
		for (Map.Entry<K, Corresponding<V>> entry : src.entrySet()) {
			map.put(entry.getKey(), (V) entry.getValue().associated());
		}
		return map;
	}

	public static Vector transform(Point2D point) {
		return Vector.newBuilder().setX(point.getX()).setY(point.getY()).build();
	}

	public interface ExtendableCorresponding extends Corresponding<Object> {

	}
}
