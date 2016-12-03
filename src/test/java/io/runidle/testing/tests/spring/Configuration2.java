package io.runidle.testing.tests.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = {"com.comcast.ace.testing.tests.spring"})
public class Configuration2 {

    @Bean
    public TestContext testContext() {
        return new TestContext();
    }

    @Bean
    @Lazy
    public TestBean2 testBean2(TestContext testContext) {
        return new TestBean2(testContext);
    }

    public static void main(String args[]) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext("com.comcast.ace.vexng.spring");

        TestBean2 testBean2 = ctx.getBean(TestBean2.class);

        System.out.println(testBean2);
    }
}
