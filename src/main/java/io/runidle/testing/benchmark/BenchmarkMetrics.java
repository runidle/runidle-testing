package io.runidle.testing.benchmark;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkMetrics {
    protected static final Logger log = logger();
    private static MetricRegistry metricRegistry = new MetricRegistry();
    private static Slf4jReporter reporter;

    static {
        reporter = Slf4jReporter.forRegistry(metricRegistry)
                .convertDurationsTo(TimeUnit.SECONDS)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .outputTo(LoggerFactory.getLogger("BenchmarkMetrics"))
                .build();
    }

    private static Logger logger() {

        Logger log = LoggerFactory.getLogger("BenchmarkMetrics");

        if (!log.isDebugEnabled() && !log.isInfoEnabled()) {
            return LoggerFactory.getLogger(BenchmarkMetrics.class);
        }
        return log;
    }

    private final static ObjectMapper mapper = new ObjectMapper().registerModule(
            new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false, MetricFilter.ALL));

    static {
        mapper.setVisibility(mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    private Timer timer;
    private String name;
    private int totalCount;
    private int concurrency;
    private boolean detail;
    private long startTime, spentTime;
    private volatile Timer.Context[] contexts;
    private volatile boolean stopped;
    private AtomicInteger finished;
    private StringBuilder emitter = new StringBuilder();
    private ScheduledExecutorService reporterExecutor = Executors.newScheduledThreadPool(1);
    private int reportInterval = 1;
    private Runnable reportTask;

    public BenchmarkMetrics(String name,
                            int concurrency,
                            int totalCount, boolean detail,
                            int reportInterval) {
        this.concurrency = concurrency;
        this.totalCount = totalCount;
        this.detail = detail;
        this.reportInterval = reportInterval;
        this.name = name;
        this.finished = new AtomicInteger();
        this.init();
    }

    private void init() {
        if (this.detail) {
            this.contexts = new Timer.Context[totalCount];
            this.timer = metricRegistry.timer(name + "_timer");
        }
        if (this.reportInterval > 0) {
            reporterExecutor = Executors.newScheduledThreadPool(1);
            this.reportTask = this::doReport;
        }
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        if (this.reporterExecutor != null) {
            this.reporterExecutor.scheduleAtFixedRate(this.reportTask,
                    this.reportInterval, this.reportInterval, TimeUnit.SECONDS);
        }
    }

    public void timeStart(int index) {
        if (this.totalCount > index)
            contexts[index] = timer.time();
    }

    public void timeStop(int index) {
        if (this.totalCount > index) {
            if (contexts[index] == null) {
                //log.error("null ----- " + index);
            } else
                contexts[index].stop();
        }
    }

    public void done(int doneCount) {
        if (this.finished.addAndGet(doneCount) >= this.totalCount) {
            this.stop();
        }
    }

    public void stop() {
        if (this.stopped) return;
        this.spentTime = System.currentTimeMillis() - this.startTime;
        this.stopped = true;
        this.reporterExecutor.shutdownNow();
    }

    public void waitFinished(int timeout) {
        while (!this.stopped) {
            try {
                Thread.sleep(1);
            } catch (Exception ex) {

            }
            if (System.currentTimeMillis() - this.startTime > timeout) {
                this.stop();
            }
        }

        if (this.spentTime > timeout) {
            log.warn("!!!!Test timeout,> " + timeout + "ms");
        }
        this.doReport();
    }

    private void doReport() {
        if (this.detail) {
            emit("timer", this.timer);
            log.info(this.emitter.toString());
            metricRegistry.remove(name + "_timer");
        } else {
            long thisSpentTime = spentTime;
            if (spentTime == 0) {
                thisSpentTime = System.currentTimeMillis() - this.startTime;
            }
            long tps = this.finished.get();
            if (thisSpentTime > 0)
                tps = tps / thisSpentTime;
            log.info(name + "(Total: {}, Concurrency: {}, SpendTime: {}): {}/ms",
                    this.finished.get(), this.concurrency, thisSpentTime, tps);
        }
    }

    public BenchmarkMetrics emit(String name, Object metric) {
        try {
            this.emitter.append("\r\n" + this.name + "_" + name + ": \r\n")
                    .append(mapper.writeValueAsString(metric));
        } catch (Exception ex) {

        }
        return this;
    }
}
