package org.seekers.grpc;

import java.io.File;
import java.io.IOException;

public interface SeekersClient extends AutoCloseable {

    void host(File file);

    void close() throws IOException;
}
