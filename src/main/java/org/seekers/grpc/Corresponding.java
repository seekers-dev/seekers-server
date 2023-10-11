package org.seekers.grpc;

import com.google.protobuf.Message;

import java.util.Collection;
import java.util.stream.Collectors;

public interface Corresponding<T> {
    T associated();

    static <T> T transform(Corresponding<T> corresponding) {
        return corresponding.associated();
    }

    static <T> Collection<T> transform(Collection<? extends Corresponding<T>> corresponding) {
        return corresponding.stream().map(Corresponding::associated).collect(Collectors.toList());
    }

    interface ExtendableCorresponding extends Corresponding<Message> {

    }
}
