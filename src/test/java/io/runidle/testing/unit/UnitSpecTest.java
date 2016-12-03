package io.runidle.testing.unit;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UnitSpecTest extends BaseUnitSpec {
    @Test
    public void testLoadYamlConfiguration() throws Exception {
        TestConfiguration configuration = loadConfiguration(TestConfiguration.class, "test-config.yaml");
        this.verifyConfiguration(configuration);
    }

    @Test
    public void testLoadPropertiesConfiguration() throws Exception {
        TestConfiguration configuration = loadConfiguration(TestConfiguration.class, "test-config.properties");
        this.verifyConfiguration(configuration);
    }

    public void verifyConfiguration(TestConfiguration configuration) {
        //log.info(toString(configuration));
        assertEquals(configuration.getName(), "testName");
        assertEquals(configuration.getCommons().size(), 2);
        assertEquals(configuration.getValues().size(), 3);
        assertEquals(configuration.getCommonMap().size(), 2);
    }
}