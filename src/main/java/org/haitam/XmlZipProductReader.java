package org.haitam;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class XmlZipProductReader {

    public void read(Path zipPath) throws IOException {

        try (
                InputStream fileInputStream = Files.newInputStream(zipPath);
                BufferedInputStream bufferedInputStream =
            new BufferedInputStream(fileInputStream);
                ZipInputStream zipInputStream =
                        new ZipInputStream(bufferedInputStream)
        ) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                if (!entry.getName()
                        .toLowerCase()
                        .endsWith(".xml")) {

                    zipInputStream.closeEntry();
                    continue;
                }

                System.out.println(
                        "XML file found: " + entry.getName()
                );

                zipInputStream.closeEntry();
            }
        }
    }
}