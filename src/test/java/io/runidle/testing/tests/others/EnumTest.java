package io.runidle.testing.tests.others;

import io.runidle.testing.unit.BaseUnitSpec;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class EnumTest extends BaseUnitSpec {

    @Test
    public void testModify() throws IllegalAccessException {

        FieldUtils.writeDeclaredField(Enum.valueOf(Type.class, "AAA"), "value", 100, true);
        assertEquals(Type.AAA.value, 100);
    }

    public interface TypeValue {
        int value();
    }

    public enum Type implements TypeValue {
        AAA(1);
        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}
