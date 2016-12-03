package io.runidle.testing.benchmark;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class BenchmarkContextTest {
    @Test
    public void test() {
        new BenchmarkContext()
                .concurrency(400).iterations(1000)
                .benchmarkTask((index, runnerContext) -> {
                    runnerContext.done(index);
                })
                .start();
    }
}