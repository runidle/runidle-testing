package io.runidle.testing.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Threads(4)
@Warmup(iterations = 0)
@Measurement(iterations = 20)
public class MicroBenchmarkBenchmark extends AbstractMicrobenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        Random random = new Random();

        @org.openjdk.jmh.annotations.Setup(Level.Iteration)
        public void setup() {
            log.info("setup ---------" + map.size());
        }

        @TearDown(Level.Iteration)
        public void teardown() {
            log.info("teardown---------" + map.size());
        }
    }

    @Benchmark
    public void testConcurrentHashMapPut(BenchmarkState state) {
        state.map.put(state.random.nextInt(), "dfd");
    }

    //@Benchmark
    public void testConcurrentHashMapPut2(BenchmarkState state) {
        state.map.put(state.random.nextInt(), "ddfd");
    }
}
