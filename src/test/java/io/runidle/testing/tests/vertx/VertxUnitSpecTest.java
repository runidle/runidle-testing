package io.runidle.testing.tests.vertx;

import io.runidle.testing.unit.BaseUnitSpec;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import org.junit.Test;

public class VertxUnitSpecTest extends BaseUnitSpec {
    @Test(timeout = 5000)
    public void testWithJunit() {
        this.runVertxHttpServer(8888);
    }

    private void runVertxHttpServer(int port) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        HttpClient httpClient = vertx.createHttpClient();
        Blocker blocker = new Blocker();
        server.requestHandler(req -> req.response().setStatusCode(200).end());
        server.listen(port, ar -> {
            blocker.assertTrue(ar.succeeded());
        });

        httpClient.get(port, "0.0.0.0", "/")
                .handler(event -> blocker.end())
                .exceptionHandler(event1 -> blocker.fail(event1.getMessage())).end();

        blocker.awaitEnd(() -> {
            httpClient.close();
            server.close();
        });
    }
}