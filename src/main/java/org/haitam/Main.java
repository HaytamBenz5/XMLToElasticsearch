package org.haitam;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("================== BENZINANE Haitam ==================");

        long startTime = System.nanoTime();

        Path zipPath = Path.of("xml.zip");

        XmlZipProductReader reader = new XmlZipProductReader();

        try (ElasticsearchProductIndexer indexer = new ElasticsearchProductIndexer()) {

            reader.read(zipPath, indexer::indexMyProduct);

        } finally {

            long endTime = System.nanoTime();
            double executionTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
            double peakHeapMemoryMb = getPeakHeapMemory() / (1024.0 * 1024.0);

            System.out.println("====================================");
            System.out.printf("Execution time   : %.3f seconds%n", executionTimeSeconds);
            System.out.printf("Peak heap memory : %.2f MB%n", peakHeapMemoryMb);
            System.out.println("====================================");
        }
    }
    private static long getPeakHeapMemory() {

        long peakMemory = 0;

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) {
                peakMemory += pool.getPeakUsage().getUsed();
            }
        }

        return peakMemory;
    }

}