package io.runidle.testing.tests.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.comcast.ace.testing.tests.spring"})
public class Configuration1 {
    @Bean
    public TestBean testBean() {
        return new TestBean();
    }

    @Bean
    public TestBean testBean3() {
        return new TestBean3();
    }
}
