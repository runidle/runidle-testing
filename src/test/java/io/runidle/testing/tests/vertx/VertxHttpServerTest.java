package io.runidle.testing.tests.vertx;

import io.runidle.testing.unit.BaseUnitSpec;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

public class VertxHttpServerTest extends BaseUnitSpec {
    private HttpServer httpServer;
    private final static Random random = new Random();
    private final static int port = 30000 + random.nextInt(30000);

    @BeforeClass
    public void setup() {
        Blocker blocker = new Blocker();
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create())
                .handler(CookieHandler.create())
        ;
        router.get("/test").handler(event -> {

            log.info("absoluteURI-----" + event.request().absoluteURI());
            log.info("path-----" + event.request().path());
            log.info("query-----" + event.request().query());
            log.info("params-----" + event.request().params());
            log.info("uri-----" + event.request().uri());
            log.info("host-----" + event.request().getHeader("host"));
            event.response().end();
        });
        httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(port))
                .requestHandler(router::accept)
                .listen(event -> {
                    log.error("", event.cause());
                    blocker.end();
                });

        blocker.awaitEnd();
        log.info("Test Server started");
    }

    @AfterClass
    public void teardown() {
        httpServer.close();
    }

    @Test
    public void testRequestValues() {
        Blocker blocker = new Blocker();
        Vertx.vertx().createHttpClient().getAbs("http://localhost:" + port + "/test?a=1&b=1")
                .handler(event -> {
                    blocker.end();
                })
                .end();
        blocker.awaitEnd();


        Blocker blocker2 = new Blocker();
        Vertx.vertx().createHttpClient(new HttpClientOptions()

                .setDefaultHost("localhost").setDefaultPort(port))
                .get(port, "localhost", "/test?a=1&b=1")
                .handler(event -> {
                    blocker2.end();
                })
                .exceptionHandler(event -> {
                    log.error("", event);
                })
                .end();
        blocker2.awaitEnd();
    }
}
