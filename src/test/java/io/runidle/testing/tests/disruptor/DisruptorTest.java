package io.runidle.testing.tests.disruptor;


import io.runidle.testing.unit.BaseUnitSpec;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import org.testng.annotations.Test;

public class DisruptorTest extends BaseUnitSpec {

    @Test
    public void testEventHandler() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            //log.info("Handled TestEvent---" + testEvent);
            blocker.end();
        });
        disruptor.start();

        for (int i = 0; i < 10; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventHandler1() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        EventHandlerGroup<TestEvent> eg = disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            testEvent.data = null;
            blocker.end();
        }).then((testEvent, l, b) -> {
            log.info("Handled TestEvent-2--" + testEvent);
            blocker.end();
        });
        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventHandler2() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        });
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent--2-" + testEvent);
            blocker.end();
        });
        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventHandler3() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        }).and(disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-2--" + testEvent);
            blocker.end();
        }));
        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventHandler4() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        //two threads
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        }, (testEvent, l, b) -> {
            log.info("Handled TestEvent--2-" + testEvent);
            blocker.end();
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventHandler5() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        }).handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-2--" + testEvent);
            blocker.end();
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventHandler6() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        });
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-2--" + testEvent);
            blocker.end();
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testCombineEventHandler1() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        });

        disruptor.handleEventsWithWorkerPool(testEvent -> {
            log.info("Handled TestEvent-2--" + testEvent);
            blocker.end();
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testCombineEventHandler2() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));


        disruptor.handleEventsWithWorkerPool(testEvent -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        });

        disruptor.handleEventsWith((testEvent, l, b) -> {
            log.info("Handled TestEvent-2--" + testEvent);
            blocker.end();
        });


        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventWorker() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        disruptor.handleEventsWithWorkerPool(testEvent -> {
            log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        });
        disruptor.handleEventsWithWorkerPool(testEvent -> {
            log.info("Handled TestEvent-2--" + testEvent);
        });
        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventWorker2() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        //one worker handle one data in two threads
        disruptor.handleEventsWithWorkerPool(testEvent -> {
            log.info("Handled TestEvent-11--" + testEvent);
            blocker.end();
        }, testEvent -> {
            log.info("Handled TestEvent-12--" + testEvent);
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventWorker3() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        //Four threads, handle data twice
        disruptor.handleEventsWithWorkerPool(testEvent -> {
            //log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        }, testEvent -> {
            //log.info("Handled TestEvent-2--" + testEvent);
        }).handleEventsWithWorkerPool(testEvent -> {
            //log.info("Handled TestEvent-3--" + testEvent);
            blocker.end();
        }, testEvent -> {
            //log.info("Handled TestEvent-4--" + testEvent);
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Test
    public void testEventWorker4() {
        BaseUnitSpec.Blocker blocker = new BaseUnitSpec.Blocker();
        Disruptor<TestEvent> disruptor = new Disruptor<>(TestEvent::new, 1024, new DefaultThreadFactory("Test-Disruptor"));
        //Four threads, handle data twice
        disruptor.handleEventsWithWorkerPool(testEvent -> {
            //log.info("Handled TestEvent-1--" + testEvent);
            blocker.end();
        }, testEvent -> {
            //log.info("Handled TestEvent-2--" + testEvent);
        });
        disruptor.handleEventsWithWorkerPool(testEvent -> {
            //log.info("Handled TestEvent-3--" + testEvent);
            blocker.end();
        }, testEvent -> {
            //log.info("Handled TestEvent-4--" + testEvent);
        });

        disruptor.start();

        for (int i = 0; i < 2; i++)
            disruptor.publishEvent((testEvent, l, o) -> testEvent.setData(o), "A_" + i);
        blocker.awaitEnd();
    }

    @Data
    private static class TestEvent {
        private String data;
    }
}
