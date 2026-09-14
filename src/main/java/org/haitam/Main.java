package org.haitam;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        Path zipPath = Path.of("xml.zip");

        XmlZipProductReader reader =
                new XmlZipProductReader();

        reader.read(
                zipPath,
                product -> System.out.println(product)
        );
    }
}