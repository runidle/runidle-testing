package io.runidle.testing.tests.rxjava;

import io.runidle.testing.benchmark.BenchmarkRunner;
import io.runidle.testing.unit.BaseUnitSpec;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.runidle.testing.benchmark.Benchmark;
import lombok.Data;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class RxjavaBenchmark extends BaseUnitSpec {

    @Test
    public void runRxjava0() {
//        benchmark(benchmarkConfig(20000, 100000, 100), index ->
//                Observable.from(new Fiber<>((SuspendableCallable<Object>) () -> index).start())
//        );

        Benchmark.benchmark().threads(1).concurrency(20000).iterations(500).rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    Observable.just(index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    @Test
    public void runRxjava01() {
        Observable observable = Observable.just(0);

        Benchmark.benchmark().threads(4).concurrency(10000).iterations(500).rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    observable
                            .doOnError(throwable -> {
                                runnerContext.done(index);
                            })
                            .doOnNext(integer -> {
                                runnerContext.done(index);
                            })
                            .subscribe();
                })
                .start();
    }

    @Test
    public void runRxjava1() {
        final int iterations = 10000000;
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        Benchmark.benchmark().concurrency(1).iterations(iterations).rounds(10)
                .benchmarkTask((iterations1, runnerContext) -> {
                    Observable.range(0, iterations1)
                            .flatMap(new Func1<Integer, Observable<TestEvent>>() {
                                @Override
                                public Observable<TestEvent> call(Integer integer) {
                                    return Observable.just(new TestEvent())
                                            //.observeOn(Schedulers.from(executor))
                                            ;
                                }
                            })
//                    .map((Func1<TestEvent, Object>) testEvent -> testEvent)
                            .subscribe(integer -> {
                                runnerContext.done(iterations1);
                            });
                })
                .start();
    }

    @Test
    public void runRxjava11() {
        final int iterations = 10000000;
        Benchmark.benchmark().concurrency(1).iterations(iterations).rounds(10)
                .benchmarkTask((iterations1, runnerContext) -> {
                    for (int i = 0; i < iterations1; i++) {
                        Observable.just(new TestEvent())
                                .map((Func1<TestEvent, Object>) testEvent -> testEvent)
                                .subscribe(integer -> {
                                    runnerContext.done(iterations1);
                                });
                    }
                })
                .start();
    }

    @Test
    public void runRxjava2() {

        final int iterations = 2000000;

        Benchmark.benchmark().threads(4).concurrency(10000).iterations(iterations).rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    Observable.just(index)
                            //.observeOn(Schedulers.computation())
                            .map((Func1<Integer, Object>) integer -> integer)
                            .observeOn(Schedulers.computation())
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            });

                })
                .start();
    }

    @Test
    public void runRxjava3() {
        final int total = 10000000;

        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
                        for (int i = 0; i < iterations; i++) {
                            subscriber.onNext(i);
                        }
                        subscriber.onCompleted();
                    }).onBackpressureBuffer()
                            .observeOn(Schedulers.computation())
                            .subscribe(integer -> {
                                runnerContext.done(iterations);
                            }, throwable -> log.error("" + throwable));
                })
                .start();
    }

    @Test
    public void runRxjava4() {
        final int total = 1000000;
        Benchmark.benchmark().concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    for (int i = 0; i < iterations; i++) {
                        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
                            subscriber.onNext(1);
                            subscriber.onCompleted();
                        })
                                .observeOn(Schedulers.computation())
                                .subscribe(integer -> {
                                    runnerContext.done(iterations);
                                }, throwable -> log.error("" + throwable));
                    }

                })
                .start();
    }

    @Test
    public void runScheduler() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Benchmark.benchmark().concurrency(10000).iterations(500).rounds(10)
                .benchmarkTask((iterations, runnerContext) -> {
                    executor.submit(() -> {
                        runnerContext.done(iterations);
                    });
                })
                .start();

        executor.shutdown();
    }

    @Test
    public void runDisruptor1() {

        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 4096, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            testEvent.runnerContext.done(testEvent.data);
        });
        disruptor.start();
        Benchmark.benchmark().threads(1).concurrency(1).iterations(10000000).rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    disruptor.publishEvent((testEvent, l, o) -> {
                        testEvent.data(index).runnerContext(runnerContext);
                    }, index);
                })
                .start();
    }

    @Test
    public void runDisruptor11() {

        final int total = 10000000;
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 4096, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            testEvent.runnerContext.done(testEvent.data);
        }).then((testEvent, sequence, endOfBatch) -> {

        });
        disruptor.start();
        Benchmark.benchmark().threads(1).concurrency(1).iterations(total).rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    disruptor.publishEvent((testEvent, l, o) -> {
                        testEvent.data(index).runnerContext(runnerContext);
                    }, index);
                })
                .start();
    }

    @Test
    public void runDisruptor2() {

        final int total = 10000;
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 4096, new DefaultThreadFactory("Test-Disruptor"));
        int threadCount = 4;

        WorkHandler<TestEvent>[] workHandlers = new WorkHandler[threadCount];
        for (int i = 0; i < threadCount; i++) {
            workHandlers[i] = o -> {
                o.runnerContext.done(o.data);
            };
        }
        disruptor.handleEventsWithWorkerPool(workHandlers)
//                .then((testEvent, l, b) -> {
//                })
        ;
        disruptor.start();
        Benchmark.benchmark().threads(4).concurrency(100).iterations(total).rounds(10)
                .warmupConcurrency(10).warmupIterations(100).warmupRounds(5)
                .benchmarkTask((index, runnerContext) -> {
                    disruptor.publishEvent((testEvent, l, o) -> {
                        testEvent.data(index).runnerContext(runnerContext);
                    }, index);
                })
                .start();

    }

    @Data
    @Accessors(fluent = true)
    private static class TestEvent {
        private BenchmarkRunner.IRunnerContext runnerContext;
        private int data;
        private static String s = "1fjlkdlfjfjdkjhhgkjjhhggfdkjhhhghjdkjfhdfhdfdhlkfkdlfeodklfjllkfikkfkdkdkdkdkfj";
        private String name0 = s;
        private String name1 = s;
        private String name2 = s;
        private String name3 = s;
        private String name4 = s;
        private String name5 = s;
        private String name6 = s;
        private String name7 = s;
        private String name8 = s;
        private String name9 = s;
        private String name10 = s;
        private String name11 = s;
        private String name12 = s;
        private String name13 = s;
        private String name14 = s;
        private String name15 = s;
        private String name16 = s;
        private String name17 = s;
        private String name18 = s;
        private String name19 = s;
        private String name20 = s;
    }

    @Test
    public void runRxjavaRxObserver() {
//       benchmark(benchmarkConfig(20000, 100000, 100), index -> Observable.just(true));
        final AtomicInteger counter = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        final int total = 10000000;
        RxObserver<Integer> observer = new RxObserver<>();
        observer.subscribe();
        for (int i = 0; i < total; i++) {
            observer.next(i);
            counter.incrementAndGet();
        }
        observer.complete();
        blockingMsUntil(3000, () -> counter.get() >= total, () -> {
        });
        log.info("Spent time:  " + (System.currentTimeMillis() - startTime));
    }


    public static class RxObserver<T> {
        private Subscriber<? super T> subscriber;
        private Observable<T> observable;

        RxObserver() {
            observable = Observable.
                    create((Observable.OnSubscribe<T>) s -> subscriber = s);
        }

        public void next(T data) {
            //log.info("1---------");
            subscriber.onNext(data);
        }

        public void subscribe() {
            observable
                    .onBackpressureBuffer()
                    .observeOn(Schedulers.computation())
                    .subscribe(
                            new Action1<T>() {
                                @Override
                                public void call(T t) {
                                    //log.info("2------");
                                }
                            }
                    );
        }

        public void complete() {
            subscriber.onCompleted();
        }
    }

    @Test
    public void runEmptyForTime() {
        Benchmark.benchmark().concurrency(1).iterations(1000000000).rounds(10)
                .benchmarkTask((index, benchmarkContext) -> {
                    benchmarkContext.done(index);
                })
                .start();
    }
}
