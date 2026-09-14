package org.haitam;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElasticsearchProductIndexer implements AutoCloseable {

    private final TransportClient client;

    private final BulkProcessor bulkProcessor;

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
        System.out.println( "==>  Elasticsearch connected: " + isConnected());

        createIndexIfNeeded();

        bulkProcessor = BulkProcessor.builder(client::bulk, new BulkProcessor.Listener() {

                            @Override
                            public void beforeBulk(long executionId, BulkRequest request) {
                                System.out.println("==> Sending bulk " + executionId + " containing " + request.numberOfActions() + " documents");
                            }

                            @Override
                            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                                if (response.hasFailures()) {
                                    System.err.println("==> Bulk " + executionId + " completed with errors");
                                } else {
                                    System.out.println("==> Bulk " + executionId + " completed successfully"
                                    );
                                }
                            }

                            @Override
                            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                                System.err.println("==> Bulk " + executionId + " failed");
                                failure.printStackTrace();
                            }
                        }
                )
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(
                                TimeValue.timeValueMillis(100),
                                5
                        )
                )
                .build();


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

        System.out.println("==> Index created: " + INDEX_NAME);
    }


    public void indexMyProduct(Map<String, Object> product) {

        Object sku = product.get("article_sku");

        if (sku == null) {
            System.err.println("Product ignored: missing article_sku");
            return;
        }

        IndexRequest request = new IndexRequest(INDEX_NAME, "_doc", sku.toString());

        request.source(product);
        bulkProcessor.add(request);
    }

    @Override
    public void close() {


        try {
            bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            client.close();
        }
    }
}