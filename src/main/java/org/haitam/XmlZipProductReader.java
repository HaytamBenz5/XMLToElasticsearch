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
import java.util.LinkedHashMap;
import java.util.Map;
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

        Map<String, Object> currentProduct = null;
        String currentField = null;
        StringBuilder currentValue = null;

        while (reader.hasNext()) {

            int event = reader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT -> {

                    String elementName = reader.getLocalName();

                    if (PRODUCT_TAG.equals(elementName)) {
                        currentProduct = new LinkedHashMap<>();
                    }
                    else if (currentProduct != null) {
                        currentField = elementName;
                        currentValue = new StringBuilder();
                    }
                }

                case XMLStreamConstants.CHARACTERS,XMLStreamConstants.CDATA -> {

                    if (currentProduct != null && currentField != null && currentValue != null) {
                        currentValue.append(reader.getText());
                    }
                }

                case XMLStreamConstants.END_ELEMENT -> {

                    String elementName = reader.getLocalName();

                    if (PRODUCT_TAG.equals(elementName)) {

                        System.out.println(currentProduct);

                        currentProduct = null;
                        currentField = null;
                        currentValue = null;

                    }
                    else if (currentProduct != null && currentField != null && currentField.equals(elementName)) {

                        String value = currentValue.toString().trim();

                        if (!value.isEmpty()) {
                            currentProduct.put(currentField, value);
                        }

                        currentField = null;
                        currentValue = null;
                    }
                }

                default -> {}
            }


        }
    }
}