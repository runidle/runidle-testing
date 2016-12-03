package io.runidle.testing.unit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import static org.testng.Assert.*;


public class BaseUnitSpec implements UnitSpec {
    protected final static Logger log = LoggerFactory.getLogger(BaseUnitSpec.class);
    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public void beforeClass() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void before() throws Exception {
        log.info("Starting test: " + this.getClass().getSimpleName() + "#" + this.name.getMethodName());
        this.doBefore();
    }

    protected void doBefore() {
    }

    @BeforeMethod
    public void beforeTest(Method method) throws Exception {
        log.info("Starting test: " + this.getClass().getSimpleName() + "#" + method.getName());
        this.doBefore();
    }

    protected void blockingSec(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception ex) {
        }
    }

    protected void blockingMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {
        }
    }

    protected static void blockingMsUntil(int ms, BooleanSupplier booleanSupplier, TimeoutAction timeoutAction) {
        long startTime = System.currentTimeMillis();
        log.info("Waiting result .....");
        while (!booleanSupplier.getAsBoolean()) {
            try {
                Thread.sleep(10);
                if (System.currentTimeMillis() - startTime > ms) {
                    timeoutAction.onTimeout();
                    return;
                }
            } catch (Exception ex) {
            }
        }
        log.info("Done.");
    }

    public interface TimeoutAction {
        void onTimeout();
    }

    protected static void blockingMsUntil(int ms, BooleanSupplier booleanSupplier) {
        long startTime = System.currentTimeMillis();
        while (!booleanSupplier.getAsBoolean()) {
            try {
                Thread.sleep(10);
                if (System.currentTimeMillis() - startTime > ms) return;
            } catch (Exception ex) {
            }
        }
    }

    protected static void blockingUntil(BooleanSupplier booleanSupplier) {
        blockingMsUntil(30000, booleanSupplier);
    }

    protected static void blockingUntil(BooleanSupplier booleanSupplier, TimeoutAction timeoutAction) {
        blockingMsUntil(30000, booleanSupplier, timeoutAction);
    }

    public static class Blocker {
        volatile boolean end;
        long startTime = System.currentTimeMillis();
        int errorCount = 0;

        public void awaitEnd() {
            BaseUnitSpec.blockingMsUntil(30000, () -> end);
            this.conclude();
        }

        public void awaitEnd(int ms) {
            BaseUnitSpec.blockingMsUntil(ms, () -> end);
            this.conclude();
        }

        public void awaitMs(int ms) {
            BaseUnitSpec.blockingMsUntil(ms, () -> end);
        }

        public void awaitSec(int ms) {
            BaseUnitSpec.blockingMsUntil(ms * 1000, () -> end);
        }

        public void awaitEnd(EndAction endAction) {
            BaseUnitSpec.blockingMsUntil(30000, () -> end);
            endAction.doEnd();
            this.conclude();
        }

        public void awaitEnd(int ms, EndAction endAction) {
            BaseUnitSpec.blockingMsUntil(ms, () -> end);
            endAction.doEnd();
            this.conclude();
        }

        public void awaitMs(int ms, EndAction endAction) {
            BaseUnitSpec.blockingMsUntil(ms, () -> end);
            endAction.doEnd();
        }

        public void awaitSec(int ms, EndAction endAction) {
            BaseUnitSpec.blockingMsUntil(ms * 1000, () -> end);
            endAction.doEnd();
        }

        public long end() {
            end = true;
            return System.currentTimeMillis() - startTime;
        }

        public void fail(String msg) {
            log.error(msg);
            this.errorCount = errorCount + 1;
        }

        public Blocker endIf(BooleanSupplier supplier) {
            if (supplier.getAsBoolean()) {
                this.end();
            }
            return this;
        }

        public Blocker endIf(boolean value) {
            if (value) {
                this.end();
            }
            return this;
        }

        public void failAndEnd(String msg) {
            this.fail(msg);
            this.end();
        }

        public void endIn(long time) {
            this.verify(() -> assertTrue(this.end() < time));
        }

        public void endOut(long time) {
            this.verify(() -> assertTrue(this.end() > time));
        }

        public Blocker assertNotEquals(Object o1, Object o2) {
            this.verify(() -> Assert.assertNotEquals(o1, o2));
            return this;
        }

        public Blocker assertEquals(Object o1, Object o2) {
            this.verify(() -> Assert.assertEquals(o1, o2));
            return this;
        }

        public Blocker assertTrue(boolean value) {
            this.verify(() -> Assert.assertTrue(value));
            return this;
        }

        public Blocker assertNotNull(Object value) {
            this.verify(() -> Assert.assertNotNull(value));
            return this;
        }

        public Blocker assertNull(Object value) {
            this.verify(() -> Assert.assertNull(value));
            return this;
        }

        public Blocker assertFalse(boolean value) {
            this.verify(() -> Assert.assertFalse(value));
            return this;
        }

        public Blocker verify(CheckAction checkAction) {
            try {
                checkAction.check();
            } catch (Error error) {
                log.error("", error);
                errorCount = errorCount + 1;
            }
            return this;
        }

        public void conclude() {
            if (this.errorCount > 0) {
                Assert.fail();
            }
        }

        public interface EndAction {
            void doEnd();
        }

        public interface CheckAction {
            void check();
        }
    }
}
