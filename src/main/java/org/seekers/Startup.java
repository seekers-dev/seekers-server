package org.seekers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The Startup class contains methods for downloading, extracting, and setting up a Python project for use with the Java
 * Seekers framework. The framework relies on Python code and stub files that need to be downloaded and placed in a
 * specific directory. Additionally, a Python environment (venv) is set up.
 *
 * @author karlz
 */
public class Startup {

    /**
     * The equivalent seekers python release site for this program.
     */
    private static final String RELEASE_SIDE = "https://github.com/seekers-dev/seekers-py/releases/download/v0.1.0/";

    private static final @Nonnull String OUTPUT_FOLDER = "seekers-py";
    private static final @Nonnull String PYTHON_FILE_NAME = "seekers-py.zip";
    private static final @Nonnull String STUBS_FILE_NAME = "stubs.zip";

    private Startup() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if a list of folders exists. All folders are required to run the clients. These are currently the seekers
     * python project, the stubs folder and venv, which is currently required to run the clients platform independent
     * with the defined dependencies. The python project and stubs folder will be downloaded and unzipped from the
     * release side. The venv files will be setup over the Java <code>ProcessBuilder</code> from python. Therefore,
     * python is required to run the project successfully.
     * <p>
     * This method should be called before any actual code was executed. It ensures that all required setup steps
     * have been done. It ensures not that this is still the case during execution, e.g. file deletion/modification
     * during runtime.
     *
     * @throws IOException If any I/O error occurs. This can possibly happen during downloading files from the release
     *                     side, unpacking them or executing a command for the venv setup.
     * @see ProcessBuilder
     * @see java.nio.channels.Channel
     */
    public static void check() throws IOException {
        File output = new File(OUTPUT_FOLDER);
        if (!output.exists()) {
            download(RELEASE_SIDE + PYTHON_FILE_NAME, PYTHON_FILE_NAME);
            unzip(PYTHON_FILE_NAME, OUTPUT_FOLDER);
        }
        if (!new File(OUTPUT_FOLDER + "/seekers/grpc/stubs").exists()) {
            download(RELEASE_SIDE + STUBS_FILE_NAME, STUBS_FILE_NAME);
            unzip(STUBS_FILE_NAME, OUTPUT_FOLDER + "/seekers/grpc/stubs");
        }
        if (!new File(OUTPUT_FOLDER + "/venv").exists()) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("python -m venv venv", null, output);
            runtime.exec("./venv/bin/activate", null, output);
            runtime.exec("./venv/bin/pip install -r requirements.txt", null, output);
        }
    }

    /**
     * Downloads a file from a URL.
     *
     * @param src The source channel
     * @param des The output filename
     * @throws IOException If any I/O error occurs. See <code>FileChannel</code>.
     * @see FileOutputStream
     * @see java.nio.channels.FileChannel#transferFrom(ReadableByteChannel, long, long)
     */
    private static void download(@Nonnull String src, @Nonnull String des) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(des)) {
            stream.getChannel().transferFrom(Channels.newChannel(new URL(src).openStream()), 0, Long.MAX_VALUE);
        }
    }

    /**
     * Extracts a locale file.
     *
     * @param src The source filename
     * @param des The output directory
     * @throws IOException If the creation or writing of a directory or file fails.
     */
    private static void unzip(@Nonnull String src, @Nonnull String des) throws IOException {
        try (FileInputStream input = new FileInputStream(src); ZipInputStream unzip = new ZipInputStream(input)) {
            File directory = new File(des);
            byte[] buffer = new byte[1024];
            ZipEntry entry = unzip.getNextEntry();
            while (entry != null) {
                File file = getCheckedFile(directory, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("Failed to create directory " + file);
                    }
                } else {
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream output = new FileOutputStream(file)) {
                        int len;
                        while ((len = unzip.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    }
                }
                entry = unzip.getNextEntry();
            }
        }
    }

    /**
     * Creates and returns a new checked File instance inside a parent. This should prevent Zip Slip. Zip Slip is a form
     * of directory traversal that can be exploited by extracting files from an archive. The premise of the directory
     * traversal vulnerability is that an attacker can gain access to parts of the file system outside the target
     * folder in which they should reside.
     *
     * @param parent The parent abstract pathname
     * @param name   The child pathname
     * @return The new File instance created from the parent abstract pathname and the child pathname.
     * @throws IOException If the entry is outside the target directory.
     */
    @CheckReturnValue
    @Nonnull
    private static File getCheckedFile(@Nonnull File parent, @Nonnull String name) throws IOException {
        File child = new File(parent, name);

        String destDirPath = parent.getCanonicalPath();
        String destFilePath = child.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target directory " + name);
        }

        return child;
    }
}
