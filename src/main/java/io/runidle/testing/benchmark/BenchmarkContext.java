package io.runidle.testing.benchmark;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Accessors(fluent = true)
@Slf4j
public class BenchmarkContext {

    String name = "Default";
    int threads = 1;
    /**
     * Here the concurrency is the number of actors,
     * Generally, the number can be set 1000
     */
    int concurrency = 1;
    /**
     * The total iterations=iterations*concurrency
     * Generally, the number can be set as 10000
     */
    int iterations = 1000;
    int rounds = 3;
    int warmupIterations;
    int warmupRounds;
    int warmupConcurrency;
    int reportIntervalSeconds = 2;
    boolean defailMetrics;
    int timeout = 30000;
    BenchmarkRunner.IBenchmarkTask benchmarkTask;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private ExecutorService executorService;

    private void checkConfig() {
        this.threads = Integer.getInteger("threads", this.threads);
        this.concurrency = Integer.getInteger("concurrency", this.concurrency);
        this.iterations = Integer.getInteger("iterations", this.iterations);
        this.rounds = Integer.getInteger("rounds", this.rounds);
        this.warmupConcurrency = Integer.getInteger("warmupConcurrency", this.warmupConcurrency);
        this.warmupIterations = Integer.getInteger("warmupIterations", this.warmupIterations);
        this.warmupRounds = Integer.getInteger("warmupRounds", this.warmupRounds);
        this.concurrency = concurrency > 0 ? concurrency : 1;
        executorService = Executors.newFixedThreadPool(threads);
        checkTotalIterations(concurrency, iterations);
        checkTotalIterations(warmupConcurrency, warmupIterations);
    }

    private void checkTotalIterations(int concurrency, int iterations) {
        if (concurrency == 0 || iterations == 0) return;
        int eachIterations = Integer.MAX_VALUE / concurrency;

        if (iterations > eachIterations) {
            throw new IllegalArgumentException("Iterations can not be >" + eachIterations
                    + ": concurrency * iterations can not >" + Integer.MAX_VALUE);
        }

        if (concurrency > 3000 || (concurrency * iterations > 30000000)) {
            log.warn("The total iterations may be too large: \r\n" +
                    "totalIterations=concurrency*iterations= " + concurrency + "*" + iterations + "=" + (concurrency * iterations));
            log.warn("The test may not be abled to finish. You may need to decrease the number of concurrency or iterations.");
            log.warn("If the test can be finished. You can ignore this warning.");
        }
    }

    public void start() {
        this.checkConfig();

        if (warmupRounds > 0 && warmupIterations > 0) {
            int setup = warmupConcurrency / warmupRounds;
            if (setup == 0) setup = 1;
            int eachConcurrency = 0;
            for (int i = 0; i < warmupRounds; i++) {
                eachConcurrency = eachConcurrency + setup;
                runTest(name + "_Warmup_" + i, eachConcurrency, warmupIterations, 1);
            }
        }
        if (rounds > 0 && iterations > 0)
            runTest(name + "_Benchmark", concurrency, iterations, this.rounds);
        executorService.shutdown();
    }

    protected void runTest(String name, int concurrency, int iterations, int rounds) {
        List<BenchmarkRunner.TestRunner> testRunners = new ArrayList<>();

        int fromIndex = 0, toIndex = 0;

        for (int i = 0; i < concurrency; i++) {
            toIndex = fromIndex + iterations;

            testRunners.add(BenchmarkRunner.createTestRunner(fromIndex, toIndex,
                    defailMetrics, this.benchmarkTask));
            fromIndex = toIndex;
        }

        for (int i = 0; i < rounds; i++) {
            final BenchmarkMetrics metricTimer = new BenchmarkMetrics(name + "_Round_" + i, concurrency,
                    toIndex, this.defailMetrics, this.reportIntervalSeconds);
            metricTimer.start();
            testRunners.forEach(iTestRunner -> {
                iTestRunner.reset(metricTimer);
                this.executorService.submit(iTestRunner::start);
            });

            metricTimer.waitFinished(this.timeout);
        }

    }
}
