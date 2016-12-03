package io.runidle.testing.tests.others;

import io.runidle.testing.unit.BaseUnitSpec;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.vertx.core.Vertx;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockTest extends BaseUnitSpec {
    WireMockServer wireMockServer;

    @BeforeClass
    protected void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterClass
    protected void teardown() {
        wireMockServer.stop();
    }

    @Test
    public void test() {
        Blocker blocker = new Blocker();
        stubFor(any(urlPathMatching("/ccr/.*"))
                .willReturn(aResponse().withHeader("Location", "http://localhost:" + wireMockServer.port() + "/origin/index.m3u8")));
        stubFor(any(urlPathMatching("/origin/.*"))
                .willReturn(aResponse().withBody("1234567890")));
        Vertx.vertx().createHttpClient()
                .getAbs("http://localhost:" + wireMockServer.port() + "/ccr/index.m3u8")
                .handler(event -> {
                    blocker.assertEquals(event.statusCode(), 200);
                    blocker.assertEquals(event.getHeader("Location"), "http://localhost:" + wireMockServer.port() + "/origin/index.m3u8");
                })
                .end();

        Vertx.vertx().createHttpClient()
                .getAbs("http://localhost:" + wireMockServer.port() + "/origin/index.m3u8")
                .handler(event -> {
                    blocker.assertEquals(event.statusCode(), 200);
                    event.bodyHandler(event1 -> {
                        blocker.assertEquals(new String(event1.getBytes()), "1234567890");
                    });
                    blocker.end();
                })
                .end();

        blocker.awaitEnd();
    }
}
