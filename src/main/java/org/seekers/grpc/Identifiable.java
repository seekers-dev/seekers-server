package org.seekers.grpc;

public interface Identifiable {
    default String getType() {
        return this.getClass().getSimpleName();
    }

    default String getIdentifier() {
        return Integer.toHexString(this.hashCode());
    }
}
