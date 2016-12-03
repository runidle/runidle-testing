
package io.runidle.testing.benchmark;

import io.runidle.testing.unit.BaseUnitSpec;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all JMH benchmarks.
 */
@Warmup(iterations = AbstractMicrobenchmark.DEFAULT_WARMUP_ITERATIONS)
@Measurement(iterations = AbstractMicrobenchmark.DEFAULT_MEASURE_ITERATIONS)
@Fork(AbstractMicrobenchmark.DEFAULT_FORKS)
@State(Scope.Thread)
public abstract class AbstractMicrobenchmark extends BaseUnitSpec {

    protected static final int DEFAULT_WARMUP_ITERATIONS = 10;
    protected static final int DEFAULT_MEASURE_ITERATIONS = 10;
    protected static final int DEFAULT_FORKS = 2;

    public static final class HarnessExecutor extends ThreadPoolExecutor {
        public HarnessExecutor(int maxThreads, String prefix) {
            super(0, maxThreads, 1L, TimeUnit.DAYS, new SynchronousQueue<Runnable>(),
                    new DefaultThreadFactory(prefix));
            System.out.println("Using harness executor");
        }
    }

    protected static final String[] JVM_ARGS = {
            "-server", "-dsa", "-da", "-ea:com.comcast.ace.vexng...", "-Xms768m", "-Xmx768m",
            "-XX:MaxDirectMemorySize=768m", "-XX:+AggressiveOpts", "-XX:+UseBiasedLocking",
            "-XX:+UseFastAccessorMethods", "-XX:+OptimizeStringConcat",
            "-XX:+HeapDumpOnOutOfMemoryError", "-Dio.netty.leakDetection.level=simple",
            "-Dharness.executor=CUSTOM",
            "-Dharness.executor.class=com.comcast.ace.vexng.benchmark.util.AbstractMicrobenchmark$HarnessExecutor"
    };

    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
    }

    @Test
    public void run() throws Exception {
        String className = getClass().getSimpleName();

        ChainedOptionsBuilder runnerOptions = new OptionsBuilder()
                .include(".*" + className + ".*")
                .jvmArgs(JVM_ARGS);

        if (getWarmupIterations() > 0) {
            runnerOptions.warmupIterations(getWarmupIterations());
        }

        if (getMeasureIterations() > 0) {
            runnerOptions.measurementIterations(getMeasureIterations());
        }

        if (getForks() > 0) {
            runnerOptions.forks(getForks());
        }

        if (getReportDir() != null) {
            String filePath = getReportDir() + className + ".json";
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            runnerOptions.resultFormat(ResultFormatType.JSON);
            runnerOptions.result(filePath);
        }

        new Runner(runnerOptions.build()).run();
    }

    protected int getWarmupIterations() {
        return SystemPropertyUtil.getInt("warmupIterations", -1);
    }

    protected int getMeasureIterations() {
        return SystemPropertyUtil.getInt("measureIterations", -1);
    }

    protected int getForks() {
        return SystemPropertyUtil.getInt("forks", -1);
    }

    protected String getReportDir() {
        return SystemPropertyUtil.get("perfReportDir");
    }

}
