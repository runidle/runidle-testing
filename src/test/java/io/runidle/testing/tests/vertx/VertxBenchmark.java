package io.runidle.testing.tests.vertx;

import io.runidle.testing.unit.BaseUnitSpec;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.runidle.testing.benchmark.Benchmark;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class VertxBenchmark extends BaseUnitSpec {
    @Test
    public void runVertxTimer() {
        final int total = 1000000;
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(4));

        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    for (int i = 0; i < total; i++) {
                        vertx.setTimer(1, event -> runnerContext.done(iterations));
                    }
                })
                .start();
    }

    @Test
    public void runScheduledTimer() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        final int total = 3000000;

        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    for (int i = 0; i < total; i++) {
                        executorService.schedule(() -> {
                            runnerContext.done(iterations);
                        }, 1, TimeUnit.MILLISECONDS);
                    }
                })
                .start();
    }

    @Test
    public void runSingleThreadScheduledTimer() {
        SingleThreadEventExecutor executorService = new DefaultEventExecutor(new DefaultThreadFactory("test"));
        final int total = 3000000;

        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    for (int i = 0; i < total; i++) {
                        executorService.schedule(() -> {
                            runnerContext.done(iterations);
                        }, 1, TimeUnit.MILLISECONDS);
                    }
                })
                .start();

    }

    @Test
    public void runEventLoopTimer() {
        DefaultEventExecutorGroup executorService = new DefaultEventExecutorGroup(4);
        final AtomicInteger counter = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        final int total = 3000000;

        for (int i = 0; i < total; i++) {
            executorService.schedule((Runnable) counter::incrementAndGet, 1, TimeUnit.MILLISECONDS);
        }

        blockingUntil(() -> counter.get() >= total, () -> {
        });
        log.info("Spent time:  " + (System.currentTimeMillis() - startTime));
    }

    @Test
    public void runNettyTimer() {
        final Timer timer = new HashedWheelTimer(new DefaultThreadFactory("Timer"), 10, TimeUnit.MILLISECONDS);

        final int total = 3000000;

        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    for (int i = 0; i < iterations; i++) {
                        timer.newTimeout(timeout -> {
                            runnerContext.done(iterations);
                        }, 1, TimeUnit.MILLISECONDS);
                    }
                })
                .start();
    }


    @Test
    public void runRxTimeout() {

        final int total = 1000000;

        //very slow, about 0.3m tps

        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    for (int i = 0; i < iterations; i++) {
                        Observable.just(i)
                                .timeout(10000, TimeUnit.MILLISECONDS)
                                .subscribe(
                                        integer -> {
                                            runnerContext.done(iterations);
                                        },
                                        throwable -> runnerContext.done(iterations)
                                );
                    }
                })
                .start();
    }
}
