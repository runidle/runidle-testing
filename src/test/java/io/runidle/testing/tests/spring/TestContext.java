package io.runidle.testing.tests.spring;

import javax.annotation.Resource;

public class TestContext {
    @Resource
    private TestBean testBean;
    @Resource
    private TestBean2 testBean2;

    @Resource
    private TestBean testBean3;
}
