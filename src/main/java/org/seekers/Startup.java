package org.seekers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Startup {

    private static final String RELEASE_SIDE = "https://github.com/seekers-dev/seekers-py/releases/download/v0.1.0/";

    private static final String OUTPUT_FOLDER = "seekers-py";
    private static final String PYTHON_FILE_NAME = "seekers-py.zip";
    private static final String STUBS_FILE_NAME = "stubs.zip";

    private Startup() {
        throw new UnsupportedOperationException();
    }

    public static void check() throws IOException {
        File output = new File(OUTPUT_FOLDER);
        if (!output.exists()) {
            download(RELEASE_SIDE + PYTHON_FILE_NAME,
                    PYTHON_FILE_NAME);
            unzip(PYTHON_FILE_NAME, OUTPUT_FOLDER);
        }
        if (!new File(OUTPUT_FOLDER + "/seekers/grpc/stubs").exists()) {
            download(RELEASE_SIDE + STUBS_FILE_NAME,
                    STUBS_FILE_NAME);
            unzip(STUBS_FILE_NAME, OUTPUT_FOLDER + "/seekers/grpc/stubs");
        }
        if (!new File(OUTPUT_FOLDER + "/venv").exists()) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("python -m venv venv", null, output);
            runtime.exec("./venv/bin/activate", null, output);
            runtime.exec("./venv/bin/pip install -r requirements.txt", null, output);
        }
    }

    private static void download(String src, String des) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(des)) {
            stream.getChannel().transferFrom(Channels.newChannel(new URL(src).
                    openStream()), 0, Long.MAX_VALUE);

        }
    }

    private static void unzip(String src, String des) throws IOException {
        try (FileInputStream input = new FileInputStream(src); ZipInputStream unzip = new ZipInputStream(input)) {

            File desDir = new File(des);
            byte[] buffer = new byte[1024];
            ZipEntry zipEntry = unzip.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(desDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream output = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = unzip.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = unzip.getNextEntry();
            }
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target directory " + zipEntry.getName());
        }

        return destFile;
    }
}
