package homework;

import homework.annotation.After;
import homework.annotation.Before;
import homework.annotation.Test;

@SuppressWarnings("java:S2187")
public class MainTest {

    @Before
    public void beforeTest() {
        System.out.println("Test setup");
    }

    @Before
    public void beforeTestAdditional() {
        System.out.println("Test additional setup");
    }

    @Test
    public void testWithoutErrors() {
        System.out.println("Running test without errors");
    }

    @Test
    public void testWithErrors() {
        System.out.println("Running test WITH errors");
        throw new RuntimeException("Error while testing");
    }

    @Test
    public void testWithoutErrors2() {
        System.out.println("Running test without errors 2");
    }

    @After
    public void afterTest() {
        System.out.println("Test cleanup");
    }

    @After
    public void afterTestAdditional() {
        System.out.println("Test additional cleanup");
    }
}
