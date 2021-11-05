package org.icule.player;

import org.junit.Assert;

import java.lang.reflect.Type;

public class TestUtils {

    public static void assertException(RunnableWithException runnable, Type exceptionType) {
        try {
            runnable.execute();
            Assert.fail("Exception wasn't throw.");
        }
        catch (Exception e) {
            if (!e.getClass().equals(exceptionType)) {
                Assert.fail("The wrong exception was issued.");
            }
        }
    }
}
