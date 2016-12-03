package io.runidle.testing.tests.vertx;

import io.runidle.testing.unit.BaseUnitSpec;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import org.testng.annotations.Test;

public class VertxUnitSpecNGTest extends BaseUnitSpec {
    @Test(timeOut = 5000)
    public void testWithTestNg() {
        this.runVertxHttpServer(8889);
    }

    @Test(timeOut = 5000)
    public void testWithTestNg2() {
        log.info("test2");
    }

    private void runVertxHttpServer(int port) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        HttpClient httpClient = vertx.createHttpClient();
        server.requestHandler(req -> req.response().setStatusCode(200).end());
        Blocker blocker1 = new Blocker();
        server.listen(port, ar -> {
            blocker1.assertTrue(ar.succeeded());
            blocker1.end();
        });
        blocker1.awaitEnd();

        Blocker blocker2 = new Blocker();
        httpClient.get(port, "0.0.0.0", "/")
                .handler(event -> blocker2.end())
                .exceptionHandler(event1 -> blocker2.fail(event1.getMessage())).end();

        blocker2.awaitEnd(() -> {
            httpClient.close();
            server.close();
        });
    }
}
