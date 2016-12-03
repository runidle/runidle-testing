# runidle-testing
Asynchronous unit test and benchmark library

For asynchronous programming, it's not easy to do the unit test.
Particularly, it's hard to do the benchmark for asynchronous program.

Runidle-Testing is designed to help for the unit tests and benchmark on asynchronous programming.

### Unit Test on RxJava programming
    public class RxJavaUnit extends BaseUnitSpec  {
        @Test
        public void testRxJava() {
            Blocker blocker = new Blocker();
            Observable.just(1)
                 .map(integer -> 1000)
                 .observeOn(Schedulers.computation())
                 .subscribe(integer -> {
                     assertEquals(integer.intValue(), 1000);
                     blocker.end();
                 });
            blocker.awaitEnd();
        }
    }
__blocker__: need to use blocker object to control the test workflow of asynchronous program

### Benchmark on RxJava Programming
    Benchmark.benchmark()
             .threads(1)        
             .concurrency(20000)
             .iterations(500)
             .rounds(10)
             .warmupConcurrency(10000)
             .warmupIterations(1000)
             .warmupRounds(1)
             .benchmarkTask((index, runnerContext) -> {
                  Observable.just(index)
                            .subscribe(integer -> {
                                    runnerContext.done(index);
                            }, throwable -> {
                                    runnerContext.done(index);
                            });
             }).start();

__threads__: how many threads to start the benchmark.For asynchronous program, maybe need more threads to start iterations  
__concurrency__:  how many actors on asynchronous benchmark. For asynchronous program, the number of actors decides the concurrency, which is not decided by the threads.  
__iterations__:   how many iterations for each actors. *This is not the total iterations. TotalIterations = concurrency***iterations*  
__rounds__: how many benchmark rounds  
__warmupConcurrency__: how many actors for warmup.  
__warmupIterations__: how many iterations for warmup.  
__warmupRounds__:  how many rounds for warmup.
 