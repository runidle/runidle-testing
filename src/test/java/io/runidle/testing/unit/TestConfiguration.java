package io.runidle.testing.unit;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class TestConfiguration {
    private String name;
    private List<String> values = new ArrayList<>();
    private List<Common> commons = new ArrayList<>();
    private ConfMap commonMap = new ConfMap();

    @Data
    public static class Common {
        private String name;
        private int port;
    }

    public static class ConfMap extends HashMap<String, Common> {

    }
}
