package org.haitam;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

public class ElasticsearchProductIndexer implements AutoCloseable {

    private final TransportClient client;


    public boolean isConnected() {
        return !client.connectedNodes().isEmpty();
    }
    public ElasticsearchProductIndexer() throws Exception {

        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .build();

        client = new PreBuiltTransportClient(settings);

        client.addTransportAddress(
                        new TransportAddress(
                        InetAddress.getByName("127.0.0.1"),
                        9300
                )
        );
    }

    @Override
    public void close() {
        client.close();
    }
}