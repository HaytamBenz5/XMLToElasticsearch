package org.haitam;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class XmlZipProductReader {

    private static final String PRODUCT_TAG = "product";

    public void read(Path zipPath) throws IOException, XMLStreamException {

        try (
                InputStream fileInputStream = Files.newInputStream(zipPath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream)
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

                System.out.println("Processing XML file: " + entry.getName());

                parseXml(zipInputStream);
                zipInputStream.closeEntry();
            }
        }
    }




    private void parseXml(InputStream inputStream) throws XMLStreamException {

        XMLInputFactory factory = XMLInputFactory.newFactory();

        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        while (reader.hasNext()) {

            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {

                String elementName = reader.getLocalName();

                if (PRODUCT_TAG.equals(elementName)) {

                    System.out.println("Product found");
                }
            }
        }
    }
}