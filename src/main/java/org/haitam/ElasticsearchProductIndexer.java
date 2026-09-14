package org.haitam;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;

public class ElasticsearchProductIndexer implements AutoCloseable {

    private final TransportClient client;

    private static final String INDEX_NAME = "products";

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
        createIndexIfNeeded();
    }


    private void createIndexIfNeeded() throws IOException {

        boolean exists = client.admin()
                .indices()
                .prepareExists(INDEX_NAME)
                .get()
                .isExists();

        if (exists) {
            return;
        }

        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")

                .startObject("article_sku")
                .field("type", "keyword")
                .endObject()

                .startObject("brand")
                .field("type", "keyword")
                .endObject()

                .startObject("brand_reference")
                .field("type", "keyword")
                .endObject()

                .startObject("category")
                .field("type", "keyword")
                .endObject()

                .startObject("ean13")
                .field("type", "keyword")
                .endObject()

                .startObject("long_title")
                .field("type", "text")
                .endObject()

                .startObject("short_title")
                .field("type", "text")
                .endObject()

                .startObject("short_description")
                .field("type", "text")
                .endObject()

                .startObject("full_description")
                .field("type", "text")
                .endObject()

                .endObject()
                .endObject();

        client.admin()
                .indices()
                .prepareCreate(INDEX_NAME)
                .setSettings(
                        Settings.builder()
                                .put("index.number_of_shards", 1)
                                .put("index.number_of_replicas", 0)
                )
                .addMapping("_doc", mapping)
                .get();

        System.out.println("Index created: " + INDEX_NAME);
    }

    @Override
    public void close() {
        client.close();
    }
}