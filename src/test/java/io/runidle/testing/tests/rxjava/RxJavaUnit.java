package io.runidle.testing.tests.rxjava;

import io.runidle.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static org.testng.Assert.assertEquals;

public class RxJavaUnit extends BaseUnitSpec {
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

